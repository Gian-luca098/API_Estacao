package com.estacao.meteorologica.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Corpo de requisição para criar/atualizar uma leitura de sensores.
 *
 * <p>{@code stationId} é opcional aqui: ao criar via
 * {@code POST /api/stations/{stationId}/readings}, a estação já vem da URL e este campo é
 * ignorado; ao atualizar via {@code PUT /api/readings/{id}}, se informado e diferente da
 * estação atual, realoca a leitura para outra estação.
 *
 * <p>Os limites de {@code humidityPct} e {@code windDirectionDeg} espelham exatamente os
 * {@code check constraints} da tabela {@code readings} na migration, garantindo um erro 400
 * legível em vez de uma exceção de violação de constraint no banco (409/500).
 */
public record ReadingRequest(

        UUID stationId,

        OffsetDateTime recordedAt,

        @NotNull(message = "A temperatura (°C) é obrigatória.")
        BigDecimal temperatureC,

        @NotNull(message = "A umidade relativa (%) é obrigatória.")
        @DecimalMin(value = "0", message = "A umidade deve ser maior ou igual a 0.")
        @DecimalMax(value = "100", message = "A umidade deve ser menor ou igual a 100.")
        BigDecimal humidityPct,

        @NotNull(message = "A pressão atmosférica (hPa) é obrigatória.")
        BigDecimal pressureHpa,

        @DecimalMin(value = "0", message = "A chuva na última hora não pode ser negativa.")
        BigDecimal rainLastHourMm,

        @DecimalMin(value = "0", message = "A chuva acumulada em 24h não pode ser negativa.")
        BigDecimal rain24hMm,

        @DecimalMin(value = "0", message = "A velocidade do vento não pode ser negativa.")
        BigDecimal windSpeedKmh,

        BigDecimal windGust24hKmh,

        BigDecimal windAvg1hKmh,

        @NotNull(message = "A direção do vento (graus) é obrigatória.")
        @DecimalMin(value = "0", message = "A direção do vento deve ser maior ou igual a 0.")
        @DecimalMax(value = "360", inclusive = false, message = "A direção do vento deve ser menor que 360.")
        BigDecimal windDirectionDeg
) {
}
