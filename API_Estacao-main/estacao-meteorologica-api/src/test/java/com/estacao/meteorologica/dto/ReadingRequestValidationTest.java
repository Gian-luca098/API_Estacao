package com.estacao.meteorologica.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa as anotações de validação de ReadingRequest isoladamente (sem contexto Spring nem
 * banco), garantindo que os limites espelham os check constraints de "readings" na migration:
 * humidity_pct entre 0 e 100, wind_direction_deg em [0, 360).
 */
class ReadingRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private ReadingRequest validRequest(BigDecimal humidityPct, BigDecimal windDirectionDeg) {
        return new ReadingRequest(
                null, null,
                new BigDecimal("24.5"),
                humidityPct,
                new BigDecimal("1013.0"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null,
                windDirectionDeg
        );
    }

    @Test
    void aceitaValoresNosLimites() {
        Set<ConstraintViolation<ReadingRequest>> violations =
                validator.validate(validRequest(new BigDecimal("100"), new BigDecimal("0")));
        assertThat(violations).isEmpty();
    }

    @Test
    void rejeitaUmidadeAcimaDe100() {
        Set<ConstraintViolation<ReadingRequest>> violations =
                validator.validate(validRequest(new BigDecimal("100.1"), new BigDecimal("0")));
        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejeitaUmidadeNegativa() {
        Set<ConstraintViolation<ReadingRequest>> violations =
                validator.validate(validRequest(new BigDecimal("-1"), new BigDecimal("0")));
        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejeitaDirecaoDoVentoIgualA360() {
        // O check constraint do banco é "< 360" (exclusivo) — 360 já é o próprio "0" na rosa dos ventos.
        Set<ConstraintViolation<ReadingRequest>> violations =
                validator.validate(validRequest(new BigDecimal("50"), new BigDecimal("360")));
        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejeitaCamposObrigatoriosNulos() {
        ReadingRequest request = new ReadingRequest(
                null, null, null, null, null, null, null, null, null, null, null);
        Set<ConstraintViolation<ReadingRequest>> violations = validator.validate(request);
        // temperatureC, humidityPct, pressureHpa e windDirectionDeg são @NotNull.
        assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }
}
