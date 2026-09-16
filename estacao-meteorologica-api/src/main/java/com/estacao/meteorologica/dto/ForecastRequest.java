package com.estacao.meteorologica.dto;

import com.estacao.meteorologica.model.enums.ForecastIcon;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Corpo de requisição para criar/atualizar um ponto de previsão. {@code stationId} segue a
 * mesma convenção de {@link ReadingRequest}: ignorado ao criar via rota aninhada, opcional
 * ao atualizar via rota direta.
 */
public record ForecastRequest(

        UUID stationId,

        @NotNull(message = "A data/hora da previsão é obrigatória.")
        OffsetDateTime forecastFor,

        @NotBlank(message = "O rótulo (ex.: '18h') é obrigatório.")
        String label,

        @NotNull(message = "A temperatura prevista (°C) é obrigatória.")
        BigDecimal temperatureC,

        @NotNull(message = "A chance de chuva (%) é obrigatória.")
        @DecimalMin(value = "0", message = "A chance de chuva deve ser maior ou igual a 0.")
        @DecimalMax(value = "100", message = "A chance de chuva deve ser menor ou igual a 100.")
        BigDecimal rainChancePct,

        @NotNull(message = "O ícone é obrigatório (sun, cloud ou cloud-rain).")
        ForecastIcon icon
) {
}
