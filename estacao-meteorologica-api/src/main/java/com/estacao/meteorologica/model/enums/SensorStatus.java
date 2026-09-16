package com.estacao.meteorologica.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status de saúde de um sensor físico (tabela {@code sensors}, coluna {@code status}).
 *
 * <p>O banco restringe a coluna com {@code check (status in ('ok', 'warning', 'offline'))}.
 * Sem tratamento especial, {@code @Enumerated(EnumType.STRING)} gravaria os nomes das
 * constantes Java ("OK", "WARNING", "OFFLINE") em maiúsculas, violando essa constraint.
 * O valor de fio em minúsculas é usado tanto no JSON quanto na coluna do banco — ver
 * {@link com.estacao.meteorologica.model.converter.SensorStatusConverter}.
 */
public enum SensorStatus {

    OK("ok"),
    WARNING("warning"),
    OFFLINE("offline");

    private final String value;

    SensorStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SensorStatus fromValue(String value) {
        for (SensorStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException(
                "Status de sensor inválido: '" + value + "'. Valores aceitos: ok, warning, offline.");
    }
}
