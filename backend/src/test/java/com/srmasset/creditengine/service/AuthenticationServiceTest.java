package com.srmasset.creditengine.service;

import com.srmasset.creditengine.dto.request.RegisterRequest;
import com.srmasset.creditengine.entity.AppUser;
import com.srmasset.creditengine.entity.UserRole;
import com.srmasset.creditengine.exception.UsernameAlreadyExistsException;
import com.srmasset.creditengine.repository.UserRepository;
import com.srmasset.creditengine.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes da camada de negocio de autenticacao. Foco em garantir que:
 * - a senha nunca e persistida em texto puro (sempre passa pelo PasswordEncoder)
 * - usuarios duplicados sao rejeitados antes de chegar ao banco
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository, passwordEncoder, authenticationManager, jwtService, 60L);
    }

    @Test
    void deveRejeitarRegistroDeUsuarioJaExistente() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("admin", "senhaForte123", UserRole.OPERATOR);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void deveArmazenarSenhaComHashNuncaEmTextoPuro() {
        when(userRepository.existsByUsername("novo.operador")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest("novo.operador", "senhaForte123", UserRole.OPERATOR);

        AppUser saved = authenticationService.register(request);

        assertThat(saved.getPasswordHash()).isNotEqualTo("senhaForte123");
        assertThat(passwordEncoder.matches("senhaForte123", saved.getPasswordHash())).isTrue();
    }
}
