import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

const TOKEN_KEY = 'srm_credit_engine_token';
const USER_KEY = 'srm_credit_engine_user';

export interface SessionUser {
  username: string;
  role: 'ADMIN' | 'OPERATOR';
}

/**
 * Centraliza o estado de autenticacao da aplicacao: login, logout,
 * leitura/escrita do token JWT e do usuario logado. O token e mantido
 * em localStorage para sobreviver a um refresh de pagina (trade-off
 * consciente: em um cenario de seguranca mais rigoroso, o ideal seria
 * um cookie httpOnly emitido pelo backend, o que exigiria suporte
 * adicional de CORS com credentials e SameSite - fora do escopo deste
 * teste, mas vale registrar como evolucao futura).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = environment.apiUrl;

  /** Signal reativo com o usuario atual, para a UI (ex: topbar) reagir ao login/logout. */
  currentUser = signal<SessionUser | null>(this.readStoredUser());

  constructor(private http: HttpClient, private router: Router) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, request).pipe(
      tap((response) => {
        localStorage.setItem(TOKEN_KEY, response.token);
        const user: SessionUser = { username: response.username, role: response.role };
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private readStoredUser(): SessionUser | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
