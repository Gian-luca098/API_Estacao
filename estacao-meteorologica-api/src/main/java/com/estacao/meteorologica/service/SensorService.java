package com.estacao.meteorologica.service;

import com.estacao.meteorologica.dto.SensorRequest;
import com.estacao.meteorologica.dto.SensorResponse;
import com.estacao.meteorologica.exception.ResourceNotFoundException;
import com.estacao.meteorologica.model.Sensor;
import com.estacao.meteorologica.model.Station;
import com.estacao.meteorologica.model.enums.SensorStatus;
import com.estacao.meteorologica.repository.SensorRepository;
import com.estacao.meteorologica.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorService {

    private final SensorRepository sensorRepository;
    private final StationRepository stationRepository;

    /** Espelha o hook useSensors(stationId). */
    public List<SensorResponse> findByStation(UUID stationId) {
        ensureStationExists(stationId);
        return sensorRepository.findByStationIdOrderByIdAsc(stationId)
                .stream().map(this::toResponse).toList();
    }

    public SensorResponse findById(Long id) {
        return toResponse(getSensorOrThrow(id));
    }

    @Transactional
    public SensorResponse create(UUID stationId, SensorRequest request) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma estação encontrada com o id '" + stationId + "'."));
        Sensor sensor = new Sensor();
        sensor.setStation(station);
        applyRequest(sensor, request);
        return toResponse(sensorRepository.save(sensor));
    }

    @Transactional
    public SensorResponse update(Long id, SensorRequest request) {
        Sensor sensor = getSensorOrThrow(id);
        if (request.stationId() != null && !request.stationId().equals(sensor.getStation().getId())) {
            Station station = stationRepository.findById(request.stationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Nenhuma estação encontrada com o id '" + request.stationId() + "'."));
            sensor.setStation(station);
        }
        applyRequest(sensor, request);
        return toResponse(sensorRepository.save(sensor));
    }

    @Transactional
    public void delete(Long id) {
        sensorRepository.delete(getSensorOrThrow(id));
    }

    private Sensor getSensorOrThrow(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhum sensor encontrado com o id '" + id + "'."));
    }

    private void ensureStationExists(UUID stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("Nenhuma estação encontrada com o id '" + stationId + "'.");
        }
    }

    private void applyRequest(Sensor sensor, SensorRequest request) {
        sensor.setName(request.name());
        sensor.setStatus(request.status() != null ? request.status() : SensorStatus.OK);
        sensor.setLastCheckedAt(request.lastCheckedAt() != null ? request.lastCheckedAt() : OffsetDateTime.now());
    }

    private SensorResponse toResponse(Sensor sensor) {
        return new SensorResponse(
                sensor.getId(),
                sensor.getStation().getId(),
                sensor.getName(),
                sensor.getStatus(),
                sensor.getLastCheckedAt()
        );
    }
}
