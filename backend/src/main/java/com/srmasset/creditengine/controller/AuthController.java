package com.srmasset.creditengine.controller;

import com.srmasset.creditengine.dto.request.LoginRequest;
import com.srmasset.creditengine.dto.request.RegisterRequest;
import com.srmasset.creditengine.dto.response.LoginResponse;
import com.srmasset.creditengine.dto.response.UserResponse;
import com.srmasset.creditengine.entity.AppUser;
import com.srmasset.creditengine.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Autenticacao e gestao de usuarios (JWT)")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuario e retorna um JWT valido por security.jwt.expiration-minutes")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Cria um novo usuario (requer role ADMIN autenticado via Bearer token)")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = authenticationService.register(request);
        UserResponse response = new UserResponse(
                user.getId(), user.getUsername(), user.getRole(), user.isEnabled(), user.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
