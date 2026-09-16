package com.estacao.meteorologica.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Corresponde ao tipo {@code Station} do front-end (src/types/weather.ts). */
public record StationResponse(
        UUID id,
        String code,
        String name,
        Double latitude,
        Double longitude,
        BigDecimal altitudeM,
        String firmwareVersion,
        Integer readingIntervalSeconds,
        OffsetDateTime createdAt
) {
}
