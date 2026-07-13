package com.srmasset.creditengine.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Tratamento de excecoes global e centralizado. Garante que nenhum erro
 * inesperado propague um stacktrace cru para o cliente e que toda
 * resposta de erro siga um contrato (ApiError) previsivel para o frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Erro de validacao", req, details);
    }

    @ExceptionHandler({CurrencyNotFoundException.class, TransactionNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler({org.springframework.security.core.AuthenticationException.class,
            org.springframework.security.authentication.BadCredentialsException.class})
    public ResponseEntity<ApiError> handleAuthentication(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos", req, List.of());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Voce nao tem permissao para executar esta acao", req, List.of());
    }

    @ExceptionHandler({UnsupportedReceivableTypeException.class, ExchangeRateNotFoundException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT,
                "A transacao foi alterada por outra operacao concorrente. Recarregue e tente novamente.",
                req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest req) {
        // Nao vazamos detalhes internos (stacktrace, mensagem de driver JDBC, etc) ao cliente.
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado. Nossa equipe foi notificada.",
                req, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req,
                                            List<String> details) {
        ApiError body = new ApiError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
                message, req.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
