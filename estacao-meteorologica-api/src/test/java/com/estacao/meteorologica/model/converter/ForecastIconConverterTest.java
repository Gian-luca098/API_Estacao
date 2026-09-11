package com.estacao.meteorologica.model.converter;

import com.estacao.meteorologica.model.enums.ForecastIcon;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Garante que o valor gravado no banco é sempre o "wire value" ("cloud-rain"), nunca o
 * nome da constante Java (CLOUD_RAIN) — é exatamente esse descompasso que violaria o
 * check constraint da coluna forecasts.icon caso o converter não existisse.
 */
class ForecastIconConverterTest {

    private final ForecastIconConverter converter = new ForecastIconConverter();

    @Test
    void convertToDatabaseColumn_usaHifenParaCloudRain() {
        assertThat(converter.convertToDatabaseColumn(ForecastIcon.CLOUD_RAIN)).isEqualTo("cloud-rain");
        assertThat(converter.convertToDatabaseColumn(ForecastIcon.SUN)).isEqualTo("sun");
        assertThat(converter.convertToDatabaseColumn(ForecastIcon.CLOUD)).isEqualTo("cloud");
    }

    @Test
    void convertToDatabaseColumn_nuloPermanceNulo() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_reconheceValorComHifen() {
        assertThat(converter.convertToEntityAttribute("cloud-rain")).isEqualTo(ForecastIcon.CLOUD_RAIN);
    }

    @Test
    void convertToEntityAttribute_valorInvalidoLancaExcecao() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("tornado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tornado");
    }
}
