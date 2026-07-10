package com.srmasset.creditengine.dto.request;

import com.srmasset.creditengine.entity.ReceivableType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload de entrada para simulacao/registro de um recebivel.
 * Todas as validacoes de forma (nao de negocio) ficam concentradas aqui,
 * via Bean Validation, para que o controller e o service nao precisem
 * repetir checagens de "campo obrigatorio" / "valor positivo".
 */
public record ReceivableRequest(

        @NotBlank(message = "nome do cedente e obrigatorio")
        @Size(max = 150)
        String assignorName,

        @NotNull(message = "tipo de recebivel e obrigatorio")
        ReceivableType receivableType,

        @NotNull(message = "valor de face e obrigatorio")
        @DecimalMin(value = "0.01", message = "valor de face deve ser positivo")
        BigDecimal faceValue,

        @NotBlank
        @Size(min = 3, max = 3, message = "codigo de moeda deve ter 3 letras (ISO 4217)")
        String faceCurrency,

        @NotBlank
        @Size(min = 3, max = 3, message = "codigo de moeda deve ter 3 letras (ISO 4217)")
        String settlementCurrency,

        @NotNull(message = "data de vencimento e obrigatoria")
        @Future(message = "data de vencimento deve ser futura")
        LocalDate dueDate
) {
}
