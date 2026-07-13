import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Restringe rotas a usuarios com role ADMIN. Assume que authGuard ja
 * garantiu que existe uma sessao autenticada - este guard adiciona a
 * checagem de autorizacao (quem pode, nao apenas quem esta logado).
 */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.currentUser()?.role === 'ADMIN') {
    return true;
  }

  router.navigate(['/painel']);
  return false;
};
