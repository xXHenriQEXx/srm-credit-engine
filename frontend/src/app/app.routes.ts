import { Routes } from '@angular/router';
import { OperatorPanelComponent } from './features/operator-panel/operator-panel.component';
import { TransactionGridComponent } from './features/transaction-grid/transaction-grid.component';
import { LoginComponent } from './features/login/login.component';
import { UserManagementComponent } from './features/user-management/user-management.component';
import { authGuard } from './core/auth/auth.guard';
import { adminGuard } from './core/auth/admin.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: 'painel', pathMatch: 'full' },
  { path: 'painel', component: OperatorPanelComponent, canActivate: [authGuard] },
  { path: 'transacoes', component: TransactionGridComponent, canActivate: [authGuard] },
  { path: 'usuarios', component: UserManagementComponent, canActivate: [authGuard, adminGuard] },
  { path: '**', redirectTo: 'painel' },
];
