package com.estacao.meteorologica.model;

import com.estacao.meteorologica.model.enums.ForecastIcon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Ponto de previsão futura exibido no painel "Previsão". Espelha {@code public.forecasts}.
 * Corresponde ao tipo {@code ForecastEntry} do front-end (src/types/weather.ts).
 */
@Entity
@Table(name = "forecasts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    @ToString.Exclude
    private Station station;

    @Column(name = "forecast_for", nullable = false)
    private OffsetDateTime forecastFor;

    @Column(nullable = false)
    private String label;

    @Column(name = "temperature_c", nullable = false)
    private BigDecimal temperatureC;

    @Column(name = "rain_chance_pct", nullable = false)
    private BigDecimal rainChancePct;

    /** Persistido como texto ("sun" | "cloud" | "cloud-rain") via ForecastIconConverter. */
    @Column(nullable = false)
    private ForecastIcon icon;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
