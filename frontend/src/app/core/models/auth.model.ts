export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: 'ADMIN' | 'OPERATOR';
  expiresInSeconds: number;
}

export type UserRole = 'ADMIN' | 'OPERATOR';

export interface RegisterRequest {
  username: string;
  password: string;
  role: UserRole;
}

export interface UserResponse {
  id: string;
  username: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
}
