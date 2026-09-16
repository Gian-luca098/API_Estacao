package com.estacao.meteorologica.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Ícone associado a um ponto de previsão (tabela {@code forecasts}, coluna {@code icon}).
 *
 * <p>O banco restringe a coluna com
 * {@code check (icon in ('sun', 'cloud', 'cloud-rain'))} — repare no hífen em
 * "cloud-rain", que não é um identificador Java válido. Por isso cada constante carrega
 * seu valor de fio (wire value) explícito, usado tanto para JSON (via {@link JsonValue}/
 * {@link JsonCreator}, na camada REST) quanto para a coluna no banco (via
 * {@link com.estacao.meteorologica.model.converter.ForecastIconConverter}, na camada JPA).
 * Isso evita gravar "CLOUD_RAIN" (nome do enum) onde o Postgres espera "cloud-rain".
 */
public enum ForecastIcon {

    SUN("sun"),
    CLOUD("cloud"),
    CLOUD_RAIN("cloud-rain");

    private final String value;

    ForecastIcon(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ForecastIcon fromValue(String value) {
        for (ForecastIcon icon : values()) {
            if (icon.value.equalsIgnoreCase(value)) {
                return icon;
            }
        }
        throw new IllegalArgumentException(
                "Ícone de previsão inválido: '" + value + "'. Valores aceitos: sun, cloud, cloud-rain.");
    }
}
