import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PagedResult, PricingResult, ReceivableRequest, TransactionResponse
} from '../models/receivable.model';

/**
 * Camada de acesso a API. Mantida separada dos componentes para que a
 * logica de apresentacao (UI) nunca fale HTTP diretamente - isolando
 * a troca de endpoint/contrato num unico lugar.
 */
@Injectable({ providedIn: 'root' })
export class CreditEngineService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  simulate(request: ReceivableRequest): Observable<PricingResult> {
    return this.http.post<PricingResult>(`${this.baseUrl}/pricing/simulate`, request);
  }

  createTransaction(request: ReceivableRequest): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.baseUrl}/transactions`, request);
  }

  getExtract(filters: {
    assignorName?: string; settlementCurrency?: string;
    from?: string; to?: string; page: number; size: number;
  }): Observable<PagedResult<TransactionResponse>> {
    let params = new HttpParams()
      .set('page', filters.page)
      .set('size', filters.size);
    if (filters.assignorName) params = params.set('assignorName', filters.assignorName);
    if (filters.settlementCurrency) params = params.set('settlementCurrency', filters.settlementCurrency);
    if (filters.from) params = params.set('from', filters.from);
    if (filters.to) params = params.set('to', filters.to);

    return this.http.get<PagedResult<TransactionResponse>>(
      `${this.baseUrl}/reports/settlement-extract`, { params });
  }
}
