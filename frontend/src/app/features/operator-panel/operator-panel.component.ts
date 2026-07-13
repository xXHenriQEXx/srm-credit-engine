import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, switchMap, takeUntil, catchError, of } from 'rxjs';
import { CreditEngineService } from '../../core/services/credit-engine.service';
import { PricingResult } from '../../core/models/receivable.model';

/**
 * Painel do Operador: input dos dados do recebivel + preview em tempo
 * real do valor liquido, via debounce sobre o formulario reativo
 * chamando o endpoint de simulacao (que NAO persiste nada).
 * A persistencia so ocorre quando o operador clica em "Confirmar liquidação".
 *
 * Todos os campos iniciam em branco, sem valores ou moedas pré-selecionadas,
 * forçando o operador a preencher explicitamente cada dado antes de simular.
 */
@Component({
  selector: 'srm-operator-panel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './operator-panel.component.html',
  styleUrl: './operator-panel.component.css'
})
export class OperatorPanelComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Tipado explicitamente (em vez de inicializado no campo da classe) porque,
  // com useDefineForClassFields (padrão em target ES2022), inicializadores de
  // campo rodam ANTES da atribuição de propriedades do construtor (this.fb).
  // Inicializar aqui e montar de fato no construtor evita o erro
  // TS2729 "Property 'fb' is used before its initialization".
  form!: FormGroup;

  preview: PricingResult | null = null;
  previewError: string | null = null;
  loadingPreview = false;

  confirming = false;
  confirmedId: string | null = null;
  confirmError: string | null = null;

  constructor(private fb: FormBuilder, private api: CreditEngineService) {
    // Todos os campos iniciam sem valor — o usuário deve preencher manualmente.
    this.form = this.fb.group({
      assignorName: ['', [Validators.required, Validators.maxLength(150)]],
      receivableType: ['', Validators.required],
      faceValue: [null, [Validators.required, Validators.min(0.01)]],
      faceCurrency: ['', Validators.required],
      settlementCurrency: ['', Validators.required],
      dueDate: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.form.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
        switchMap(() => {
          if (this.form.invalid) {
            this.preview = null;
            this.loadingPreview = false;
            return of(null);
          }
          this.loadingPreview = true;
          this.previewError = null;
          return this.api.simulate(this.form.getRawValue() as any).pipe(
            catchError((err) => {
              this.previewError = err?.error?.message ?? 'Não foi possível calcular a simulação.';
              return of(null);
            })
          );
        }),
        takeUntil(this.destroy$)
      )
      .subscribe((result) => {
        this.loadingPreview = false;
        if (result) this.preview = result;
      });

    // Não dispara preview inicial — formulário começa intencionalmente vazio.
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  confirmSettlement(): void {
    if (this.form.invalid) return;
    this.confirming = true;
    this.confirmError = null;
    this.confirmedId = null;

    this.api.createTransaction(this.form.getRawValue() as any).subscribe({
      next: (tx) => {
        this.confirming = false;
        this.confirmedId = tx.id;
      },
      error: (err) => {
        this.confirming = false;
        this.confirmError = err?.error?.message ?? 'Erro ao confirmar a liquidação.';
      }
    });
  }

  clearForm(): void {
    // Reseta para o estado inicial: todos os campos em branco.
    this.form.reset({
      assignorName: '',
      receivableType: '',
      faceValue: null,
      faceCurrency: '',
      settlementCurrency: '',
      dueDate: '',
    });
    this.preview = null;
    this.previewError = null;
    this.confirmedId = null;
    this.confirmError = null;
  }
}
