package com.estacao.meteorologica.repository;

import com.estacao.meteorologica.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    /** Espelha o hook useSensors(stationId): sensores da estação, ordenados por id asc. */
    List<Sensor> findByStationIdOrderByIdAsc(UUID stationId);
}
