package com.estacao.meteorologica.dto;

import com.estacao.meteorologica.model.enums.ForecastIcon;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Corresponde ao tipo {@code ForecastEntry} do front-end (src/types/weather.ts). */
public record ForecastResponse(
        Long id,
        UUID stationId,
        OffsetDateTime forecastFor,
        String label,
        BigDecimal temperatureC,
        BigDecimal rainChancePct,
        ForecastIcon icon
) {
}
