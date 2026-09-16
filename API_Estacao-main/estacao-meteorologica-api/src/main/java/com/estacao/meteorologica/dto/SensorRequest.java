package com.estacao.meteorologica.dto;

import com.estacao.meteorologica.model.enums.SensorStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Corpo de requisição para criar/atualizar um sensor. {@code status} e {@code lastCheckedAt}
 * são opcionais: quando omitidos, o service aplica {@code OK} e "agora", respectivamente —
 * os mesmos defaults da coluna no banco.
 */
public record SensorRequest(

        UUID stationId,

        @NotBlank(message = "O nome do sensor é obrigatório.")
        String name,

        SensorStatus status,

        OffsetDateTime lastCheckedAt
) {
}
