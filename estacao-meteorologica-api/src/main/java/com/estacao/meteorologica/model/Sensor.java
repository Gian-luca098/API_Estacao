package com.estacao.meteorologica.model;

import com.estacao.meteorologica.model.enums.SensorStatus;
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

import java.time.OffsetDateTime;

/**
 * Status de saúde de um sensor físico da estação. Espelha {@code public.sensors}.
 * Corresponde ao tipo {@code SensorHealth} do front-end (src/types/weather.ts).
 */
@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    @ToString.Exclude
    private Station station;

    @Column(nullable = false)
    private String name;

    /** Persistido como texto ("ok" | "warning" | "offline") via SensorStatusConverter. */
    @Column(nullable = false)
    @Builder.Default
    private SensorStatus status = SensorStatus.OK;

    @Column(name = "last_checked_at", nullable = false)
    private OffsetDateTime lastCheckedAt;

    @PrePersist
    void prePersist() {
        if (lastCheckedAt == null) {
            lastCheckedAt = OffsetDateTime.now();
        }
    }
}
