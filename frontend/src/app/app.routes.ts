import { Routes } from '@angular/router';
import { OperatorPanelComponent } from './features/operator-panel/operator-panel.component';
import { TransactionGridComponent } from './features/transaction-grid/transaction-grid.component';

export const routes: Routes = [
  { path: '', redirectTo: 'painel', pathMatch: 'full' },
  { path: 'painel', component: OperatorPanelComponent },
  { path: 'transacoes', component: TransactionGridComponent },
];
