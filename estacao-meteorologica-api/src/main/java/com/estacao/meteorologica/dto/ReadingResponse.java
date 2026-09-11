package com.estacao.meteorologica.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Corresponde ao tipo {@code WeatherReading} do front-end (src/types/weather.ts) — os
 * mesmos nomes de campo em camelCase, para que o shape do JSON fique o mais próximo
 * possível do que os hooks React já esperam.
 */
public record ReadingResponse(
        Long id,
        UUID stationId,
        OffsetDateTime recordedAt,
        BigDecimal temperatureC,
        BigDecimal humidityPct,
        BigDecimal pressureHpa,
        BigDecimal rainLastHourMm,
        BigDecimal rain24hMm,
        BigDecimal windSpeedKmh,
        BigDecimal windGust24hKmh,
        BigDecimal windAvg1hKmh,
        BigDecimal windDirectionDeg
) {
}
