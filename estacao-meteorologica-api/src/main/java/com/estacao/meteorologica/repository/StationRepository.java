package com.estacao.meteorologica.repository;

import com.estacao.meteorologica.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StationRepository extends JpaRepository<Station, UUID> {

    /** Espelha o hook useStation(stationCode) do front-end. */
    Optional<Station> findByCode(String code);

    boolean existsByCode(String code);

    /** Usado na atualização, para permitir manter o próprio código ao editar uma estação. */
    boolean existsByCodeAndIdNot(String code, UUID id);
}
