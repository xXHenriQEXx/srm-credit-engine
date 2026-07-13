import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { CreditEngineService } from '../../core/services/credit-engine.service';
import { AuthService } from '../../core/auth/auth.service';
import { TransactionResponse } from '../../core/models/receivable.model';

/**
 * Grid de transacoes com paginacao SERVER-SIDE (nao carregamos a base
 * inteira no browser) e filtros dinamicos, refletindo diretamente os
 * parametros aceitos pela rota /reports/settlement-extract (SQL nativo).
 *
 * Features adicionadas:
 * - Exportacao CSV com os filtros ativos (sem paginacao)
 * - Filtro por operador (createdBy), visivel apenas para ADMIN
 * - Coluna "Operador", visivel apenas para ADMIN
 */
@Component({
  selector: 'srm-transaction-grid',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transaction-grid.component.html',
  styleUrl: './transaction-grid.component.css'
})
export class TransactionGridComponent implements OnInit {
  // Ver comentário equivalente em OperatorPanelComponent: montamos o form
  // no construtor (e não como inicializador de campo) para evitar o erro
  // TS2729 causado pela ordem de inicialização de campos vs. propriedades
  // de parâmetro do construtor sob useDefineForClassFields.
  filterForm!: FormGroup;

  rows: TransactionResponse[] = [];
  page = 0;
  size = 20;
  totalElements = 0;
  totalPages = 0;
  loading = false;
  errorMsg: string | null = null;

  exportingCsv = false;

  /** true quando o usuário logado é ADMIN */
  isAdmin = computed(() => this.authService.currentUser()?.role === 'ADMIN');

  constructor(
    private fb: FormBuilder,
    private api: CreditEngineService,
    private authService: AuthService
  ) {
    this.filterForm = this.fb.group({
      assignorName: [''],
      settlementCurrency: [''],
      from: [''],
      to: [''],
      createdBy: ['']   // filtro de operador — enviado ao backend; visível apenas para ADMIN
    });
  }

  ngOnInit(): void {
    this.load();

    this.filterForm.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)))
      .subscribe(() => {
        this.page = 0;
        this.load();
      });
  }

  load(): void {
    this.loading = true;
    this.errorMsg = null;
    const f = this.filterForm.value;

    this.api.getExtract({
      assignorName:       f.assignorName       || undefined,
      settlementCurrency: f.settlementCurrency || undefined,
      from:               f.from               || undefined,
      to:                 f.to                 || undefined,
      createdBy:          f.createdBy          || undefined,
      page: this.page,
      size: this.size
    }).subscribe({
      next: (result) => {
        this.rows = result.content;
        this.totalElements = result.totalElements;
        this.totalPages = result.totalPages;
        this.loading = false;
      },
      error: () => {
        this.errorMsg = 'Não foi possível carregar o extrato de liquidação.';
        this.loading = false;
      }
    });
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) { this.page++; this.load(); }
  }

  prevPage(): void {
    if (this.page > 0) { this.page--; this.load(); }
  }

  /**
   * Exporta todos os registros filtrados (sem paginacao) como CSV e dispara
   * o download no browser sem necessidade de endpoint dedicado no backend.
   *
   * Estrategia: busca com size=10000 (limite razoavel para um CSV operacional)
   * para nao sobrecarregar o banco nem o browser com datasets gigantes.
   * Em producao com volumes maiores, considerar um endpoint de streaming
   * dedicado no backend retornando text/csv diretamente.
   */
  exportCsv(): void {
    this.exportingCsv = true;
    const f = this.filterForm.value;

    this.api.getExtract({
      assignorName:       f.assignorName       || undefined,
      settlementCurrency: f.settlementCurrency || undefined,
      from:               f.from               || undefined,
      to:                 f.to                 || undefined,
      createdBy:          f.createdBy          || undefined,
      page: 0,
      size: 10000
    }).subscribe({
      next: (result) => {
        this.exportingCsv = false;
        this.triggerCsvDownload(result.content);
      },
      error: () => {
        this.exportingCsv = false;
        this.errorMsg = 'Não foi possível gerar o arquivo CSV.';
      }
    });
  }

  private triggerCsvDownload(data: TransactionResponse[]): void {
    const receivableTypeLabel: Record<string, string> = {
      DUPLICATA_MERCANTIL: 'Duplicata Mercantil',
      CHEQUE_PRE_DATADO:   'Cheque Pré-datado'
    };

    const headers = [
      'ID', 'Cedente', 'Tipo', 'Valor de Face', 'Moeda do Título',
      'Moeda de Liquidação', 'Valor Líquido', 'Vencimento', 'Status',
      'Criado em', 'Operador'
    ];

    const rows = data.map(tx => [
      tx.id,
      this.escapeCsvField(tx.assignorName),
      receivableTypeLabel[tx.receivableType] ?? tx.receivableType,
      tx.faceValue.toFixed(2),
      tx.faceCurrency,
      tx.settlementCurrency,
      tx.settlementValue.toFixed(2),
      tx.dueDate,
      tx.status,
      new Date(tx.createdAt).toLocaleString('pt-BR'),
      this.escapeCsvField(tx.createdBy ?? '')
    ]);

    const csvContent = [headers, ...rows]
      .map(row => row.join(';'))
      .join('\r\n');

    // BOM UTF-8 (\uFEFF) garante que Excel abre o arquivo corretamente
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const url  = URL.createObjectURL(blob);
    const date = new Date().toISOString().substring(0, 10);

    const link = document.createElement('a');
    link.href     = url;
    link.download = `transacoes_${date}.csv`;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    // Revoga a URL imediatamente após o download para liberar memória
    setTimeout(() => URL.revokeObjectURL(url), 100);
  }

  /** Envolve campos que contenham vírgula, ponto-e-vírgula ou aspas em aspas duplas. */
  private escapeCsvField(value: string): string {
    if (/[;,"\n\r]/.test(value)) {
      return `"${value.replace(/"/g, '""')}"`;
    }
    return value;
  }
}
