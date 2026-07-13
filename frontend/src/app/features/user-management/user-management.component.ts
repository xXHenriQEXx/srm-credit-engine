import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreditEngineService } from '../../core/services/credit-engine.service';
import { UserResponse } from '../../core/models/auth.model';

/**
 * Tela restrita a ADMIN para criacao de novos usuarios (operadores ou
 * outros administradores), consumindo POST /api/v1/auth/register.
 */
@Component({
  selector: 'srm-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent {
  form!: ReturnType<FormBuilder['group']>;

  creating = false;
  errorMsg: string | null = null;
  createdUsers: UserResponse[] = [];

  constructor(private fb: FormBuilder, private api: CreditEngineService) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['OPERATOR', Validators.required],
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.creating = true;
    this.errorMsg = null;

    this.api.createUser(this.form.getRawValue() as any).subscribe({
      next: (user) => {
        this.creating = false;
        this.createdUsers.unshift(user);
        this.form.reset({ username: '', password: '', role: 'OPERATOR' });
      },
      error: (err) => {
        this.creating = false;
        this.errorMsg = err?.error?.message ?? 'Não foi possível criar o usuário.';
      }
    });
  }
}
