package com.estacao.meteorologica.dto;

import com.estacao.meteorologica.model.enums.SensorStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Corresponde ao tipo {@code SensorHealth} do front-end (src/types/weather.ts). */
public record SensorResponse(
        Long id,
        UUID stationId,
        String name,
        SensorStatus status,
        OffsetDateTime lastCheckedAt
) {
}
