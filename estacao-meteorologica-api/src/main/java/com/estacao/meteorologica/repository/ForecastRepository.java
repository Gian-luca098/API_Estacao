package com.estacao.meteorologica.repository;

import com.estacao.meteorologica.model.Forecast;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    /** Listagem completa, em ordem cronológica (forecast_for asc). */
    List<Forecast> findByStationIdOrderByForecastForAsc(UUID stationId);

    /**
     * Espelha o hook useForecast(stationId, limit=6): mesmos filtro/ordenação acima,
     * mas limitado via Pageable (ex.: PageRequest.of(0, 6)).
     */
    List<Forecast> findByStationIdOrderByForecastForAsc(UUID stationId, Pageable pageable);
}
