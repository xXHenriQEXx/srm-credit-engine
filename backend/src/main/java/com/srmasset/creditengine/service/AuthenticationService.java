package com.srmasset.creditengine.service;

import com.srmasset.creditengine.dto.request.LoginRequest;
import com.srmasset.creditengine.dto.request.RegisterRequest;
import com.srmasset.creditengine.dto.response.LoginResponse;
import com.srmasset.creditengine.entity.AppUser;
import com.srmasset.creditengine.exception.UsernameAlreadyExistsException;
import com.srmasset.creditengine.repository.UserRepository;
import com.srmasset.creditengine.security.AppUserDetails;
import com.srmasset.creditengine.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Camada de negocio de autenticacao/gestao de usuarios. Mantida separada
 * do AuthController para que regras como "nao permitir username
 * duplicado" ou "hash de senha antes de persistir" nao vazem para a
 * camada de apresentacao.
 */
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long expirationMinutes;

    public AuthenticationService(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  AuthenticationManager authenticationManager,
                                  JwtService jwtService,
                                  @Value("${security.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.expirationMinutes = expirationMinutes;
    }

    public LoginResponse login(LoginRequest request) {
        // Delega a validacao de usuario/senha ao AuthenticationManager (DaoAuthenticationProvider),
        // que por sua vez usa o PasswordEncoder para comparar o hash BCrypt - nunca comparamos
        // senha em texto puro na aplicacao.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(); // nao deveria acontecer: authenticate() ja validou a existencia

        String token = jwtService.generateToken(new AppUserDetails(user), user.getRole().name());

        return new LoginResponse(token, user.getUsername(), user.getRole().name(), expirationMinutes * 60);
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        AppUser user = AppUser.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(true)
                .createdAt(OffsetDateTime.now())
                .build();

        return userRepository.save(user);
    }
}
