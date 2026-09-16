package com.estacao.meteorologica.repository;

import com.estacao.meteorologica.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    /**
     * Espelha o hook useLatestReading(stationId): última leitura da estação,
     * ordenada por recorded_at desc, limitada a 1 (Spring Data traduz
     * "findFirst...OrderBy...Desc" em um LIMIT 1 no SQL gerado).
     */
    Optional<Reading> findFirstByStationIdOrderByRecordedAtDesc(UUID stationId);

    /**
     * Espelha o hook useReadingsHistory(stationId, hours): leituras da janela
     * [now-hours, now], em ordem crescente de tempo.
     */
    List<Reading> findByStationIdAndRecordedAtGreaterThanEqualOrderByRecordedAtAsc(
            UUID stationId, OffsetDateTime since);

    /** Listagem geral (mais recentes primeiro) para uma estação, sem janela de tempo. */
    List<Reading> findByStationIdOrderByRecordedAtDesc(UUID stationId);
}
