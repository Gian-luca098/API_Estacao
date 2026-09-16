package com.estacao.meteorologica.service;

import com.estacao.meteorologica.dto.StationRequest;
import com.estacao.meteorologica.dto.StationResponse;
import com.estacao.meteorologica.exception.DuplicateResourceException;
import com.estacao.meteorologica.exception.ResourceNotFoundException;
import com.estacao.meteorologica.model.Station;
import com.estacao.meteorologica.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationService {

    private static final int DEFAULT_READING_INTERVAL_SECONDS = 60;

    private final StationRepository stationRepository;

    public List<StationResponse> findAll() {
        return stationRepository.findAll().stream().map(this::toResponse).toList();
    }

    public StationResponse findById(UUID id) {
        return toResponse(getStationOrThrow(id));
    }

    /** Espelha o hook useStation(stationCode) do front-end. */
    public StationResponse findByCode(String code) {
        Station station = stationRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma estação encontrada com o código '" + code + "'."));
        return toResponse(station);
    }

    @Transactional
    public StationResponse create(StationRequest request) {
        if (stationRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException(
                    "Já existe uma estação cadastrada com o código '" + request.code() + "'.");
        }
        Station station = new Station();
        applyRequest(station, request);
        return toResponse(stationRepository.save(station));
    }

    @Transactional
    public StationResponse update(UUID id, StationRequest request) {
        Station station = getStationOrThrow(id);
        if (stationRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException(
                    "Já existe outra estação cadastrada com o código '" + request.code() + "'.");
        }
        applyRequest(station, request);
        return toResponse(stationRepository.save(station));
    }

    /** Exclui a estação; readings/forecasts/sensors associados são removidos em cascata pelo banco. */
    @Transactional
    public void delete(UUID id) {
        stationRepository.delete(getStationOrThrow(id));
    }

    private Station getStationOrThrow(UUID id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma estação encontrada com o id '" + id + "'."));
    }

    private void applyRequest(Station station, StationRequest request) {
        station.setCode(request.code());
        station.setName(request.name());
        station.setLatitude(request.latitude());
        station.setLongitude(request.longitude());
        station.setAltitudeM(request.altitudeM());
        station.setFirmwareVersion(request.firmwareVersion());
        station.setReadingIntervalSeconds(
                request.readingIntervalSeconds() != null
                        ? request.readingIntervalSeconds()
                        : DEFAULT_READING_INTERVAL_SECONDS);
    }

    private StationResponse toResponse(Station station) {
        return new StationResponse(
                station.getId(),
                station.getCode(),
                station.getName(),
                station.getLatitude(),
                station.getLongitude(),
                station.getAltitudeM(),
                station.getFirmwareVersion(),
                station.getReadingIntervalSeconds(),
                station.getCreatedAt()
        );
    }
}
