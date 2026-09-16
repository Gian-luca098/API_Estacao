package com.estacao.meteorologica.model;

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
 * Leitura bruta dos sensores em um ciclo (padrão: 60s). Espelha {@code public.readings},
 * a fonte de verdade em série temporal do painel. Corresponde ao tipo {@code WeatherReading}
 * do front-end (src/types/weather.ts).
 */
@Entity
@Table(name = "readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    @ToString.Exclude
    private Station station;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Column(name = "temperature_c", nullable = false)
    private BigDecimal temperatureC;

    @Column(name = "humidity_pct", nullable = false)
    private BigDecimal humidityPct;

    @Column(name = "pressure_hpa", nullable = false)
    private BigDecimal pressureHpa;

    @Column(name = "rain_last_hour_mm", nullable = false)
    @Builder.Default
    private BigDecimal rainLastHourMm = BigDecimal.ZERO;

    @Column(name = "rain_24h_mm", nullable = false)
    @Builder.Default
    private BigDecimal rain24hMm = BigDecimal.ZERO;

    @Column(name = "wind_speed_kmh", nullable = false)
    @Builder.Default
    private BigDecimal windSpeedKmh = BigDecimal.ZERO;

    /** Nullable no banco: nem toda estação envia rajada máxima de 24h. */
    @Column(name = "wind_gust_24h_kmh")
    private BigDecimal windGust24hKmh;

    /** Nullable no banco: nem toda estação envia média de vento de 1h. */
    @Column(name = "wind_avg_1h_kmh")
    private BigDecimal windAvg1hKmh;

    @Column(name = "wind_direction_deg", nullable = false)
    private BigDecimal windDirectionDeg;

    @PrePersist
    void prePersist() {
        if (recordedAt == null) {
            recordedAt = OffsetDateTime.now();
        }
    }
}
