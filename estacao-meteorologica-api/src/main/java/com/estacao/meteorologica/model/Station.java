package com.estacao.meteorologica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Metadados de uma estação meteorológica física. Espelha a tabela {@code public.stations}.
 *
 * <p>Não mantém coleções {@code @OneToMany} para readings/forecasts/sensors de propósito:
 * a API expõe esses recursos via seus próprios repositórios/endpoints (mais previsível para
 * séries temporais potencialmente grandes, como {@code readings}, e evita N+1 e ciclos de
 * serialização). A exclusão em cascata continua garantida no nível do banco pelo
 * {@code on delete cascade} definido na migration.
 */
@Entity
@Table(name = "stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Station {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "altitude_m", nullable = false)
    private BigDecimal altitudeM;

    @Column(name = "firmware_version", nullable = false)
    private String firmwareVersion;

    @Column(name = "reading_interval_seconds", nullable = false)
    @Builder.Default
    private Integer readingIntervalSeconds = 60;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
