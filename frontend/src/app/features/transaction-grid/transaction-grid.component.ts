import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { CreditEngineService } from '../../core/services/credit-engine.service';
import { TransactionResponse } from '../../core/models/receivable.model';

/**
 * Grid de transacoes com paginacao SERVER-SIDE (nao carregamos a base
 * inteira no browser) e filtros dinamicos, refletindo diretamente os
 * parametros aceitos pela rota /reports/settlement-extract (SQL nativo).
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
  size = 10;
  totalElements = 0;
  totalPages = 0;
  loading = false;
  errorMsg: string | null = null;

  constructor(private fb: FormBuilder, private api: CreditEngineService) {
    this.filterForm = this.fb.group({
      assignorName: [''],
      settlementCurrency: [''],
      from: [''],
      to: ['']
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
      assignorName: f.assignorName || undefined,
      settlementCurrency: f.settlementCurrency || undefined,
      from: f.from || undefined,
      to: f.to || undefined,
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
}
