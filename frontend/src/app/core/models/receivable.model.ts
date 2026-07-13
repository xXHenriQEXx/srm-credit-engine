export type ReceivableType = 'DUPLICATA_MERCANTIL' | 'CHEQUE_PRE_DATADO';

export interface ReceivableRequest {
  assignorName: string;
  receivableType: ReceivableType;
  faceValue: number;
  faceCurrency: string;
  settlementCurrency: string;
  dueDate: string; // ISO yyyy-MM-dd
}

export interface PricingResult {
  faceValue: number;
  faceCurrency: string;
  termMonths: number;
  baseRateApplied: number;
  spreadApplied: number;
  presentValueInFaceCurrency: number;
  exchangeRateApplied: number;
  settlementCurrency: string;
  settlementValue: number;
}

export interface TransactionResponse {
  id: string;
  assignorName: string;
  receivableType: ReceivableType;
  faceValue: number;
  faceCurrency: string;
  settlementCurrency: string;
  dueDate: string;
  settlementValue: number;
  status: 'PENDING' | 'SETTLED' | 'CANCELLED';
  createdAt: string;
  createdBy: string;
}

export interface PagedResult<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
