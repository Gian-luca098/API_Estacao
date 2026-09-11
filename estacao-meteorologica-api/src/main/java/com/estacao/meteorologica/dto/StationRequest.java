package com.estacao.meteorologica.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Corpo de requisição para criar/atualizar uma estação (POST e PUT usam o mesmo shape,
 * já que PUT aqui representa substituição completa do recurso).
 *
 * <p>{@code readingIntervalSeconds} é opcional: quando omitido (null), o service aplica o
 * padrão de 60s, o mesmo default da coluna no banco.
 */
public record StationRequest(

        @NotBlank(message = "O código da estação é obrigatório.")
        @Size(max = 50, message = "O código deve ter no máximo 50 caracteres.")
        String code,

        @NotBlank(message = "O nome da estação é obrigatório.")
        @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
        String name,

        @NotNull(message = "A latitude é obrigatória.")
        @DecimalMin(value = "-90.0", message = "A latitude deve ser maior ou igual a -90.")
        @DecimalMax(value = "90.0", message = "A latitude deve ser menor ou igual a 90.")
        Double latitude,

        @NotNull(message = "A longitude é obrigatória.")
        @DecimalMin(value = "-180.0", message = "A longitude deve ser maior ou igual a -180.")
        @DecimalMax(value = "180.0", message = "A longitude deve ser menor ou igual a 180.")
        Double longitude,

        @NotNull(message = "A altitude (em metros) é obrigatória.")
        BigDecimal altitudeM,

        @NotBlank(message = "A versão do firmware é obrigatória.")
        String firmwareVersion,

        @Positive(message = "O intervalo de leitura deve ser positivo.")
        Integer readingIntervalSeconds
) {
}
