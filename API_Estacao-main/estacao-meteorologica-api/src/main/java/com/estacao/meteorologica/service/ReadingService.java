package com.estacao.meteorologica.service;

import com.estacao.meteorologica.dto.ReadingRequest;
import com.estacao.meteorologica.dto.ReadingResponse;
import com.estacao.meteorologica.exception.ResourceNotFoundException;
import com.estacao.meteorologica.model.Reading;
import com.estacao.meteorologica.model.Station;
import com.estacao.meteorologica.repository.ReadingRepository;
import com.estacao.meteorologica.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingService {

    private final ReadingRepository readingRepository;
    private final StationRepository stationRepository;

    /** Espelha o hook useLatestReading(stationId). */
    public ReadingResponse findLatestByStation(UUID stationId) {
        ensureStationExists(stationId);
        Reading reading = readingRepository.findFirstByStationIdOrderByRecordedAtDesc(stationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma leitura encontrada para esta estação ainda."));
        return toResponse(reading);
    }

    /** Espelha o hook useReadingsHistory(stationId, hours). */
    public List<ReadingResponse> findHistoryByStation(UUID stationId, int hours) {
        ensureStationExists(stationId);
        OffsetDateTime since = OffsetDateTime.now().minusHours(hours);
        return readingRepository
                .findByStationIdAndRecordedAtGreaterThanEqualOrderByRecordedAtAsc(stationId, since)
                .stream().map(this::toResponse).toList();
    }

    /** Listagem geral (mais recentes primeiro) de todas as leituras de uma estação. */
    public List<ReadingResponse> findAllByStation(UUID stationId) {
        ensureStationExists(stationId);
        return readingRepository.findByStationIdOrderByRecordedAtDesc(stationId)
                .stream().map(this::toResponse).toList();
    }

    /** Listagem paginada entre todas as estações — readings é a tabela que mais cresce. */
    public Page<ReadingResponse> findAll(Pageable pageable) {
        return readingRepository.findAll(pageable).map(this::toResponse);
    }

    public ReadingResponse findById(Long id) {
        return toResponse(getReadingOrThrow(id));
    }

    @Transactional
    public ReadingResponse create(UUID stationId, ReadingRequest request) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma estação encontrada com o id '" + stationId + "'."));
        Reading reading = new Reading();
        reading.setStation(station);
        applyRequest(reading, request);
        return toResponse(readingRepository.save(reading));
    }

    @Transactional
    public ReadingResponse update(Long id, ReadingRequest request) {
        Reading reading = getReadingOrThrow(id);
        if (request.stationId() != null && !request.stationId().equals(reading.getStation().getId())) {
            Station station = stationRepository.findById(request.stationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Nenhuma estação encontrada com o id '" + request.stationId() + "'."));
            reading.setStation(station);
        }
        applyRequest(reading, request);
        return toResponse(readingRepository.save(reading));
    }

    @Transactional
    public void delete(Long id) {
        readingRepository.delete(getReadingOrThrow(id));
    }

    private Reading getReadingOrThrow(Long id) {
        return readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma leitura encontrada com o id '" + id + "'."));
    }

    private void ensureStationExists(UUID stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("Nenhuma estação encontrada com o id '" + stationId + "'.");
        }
    }

    private void applyRequest(Reading reading, ReadingRequest request) {
        reading.setRecordedAt(request.recordedAt() != null ? request.recordedAt() : OffsetDateTime.now());
        reading.setTemperatureC(request.temperatureC());
        reading.setHumidityPct(request.humidityPct());
        reading.setPressureHpa(request.pressureHpa());
        reading.setRainLastHourMm(request.rainLastHourMm() != null ? request.rainLastHourMm() : BigDecimal.ZERO);
        reading.setRain24hMm(request.rain24hMm() != null ? request.rain24hMm() : BigDecimal.ZERO);
        reading.setWindSpeedKmh(request.windSpeedKmh() != null ? request.windSpeedKmh() : BigDecimal.ZERO);
        reading.setWindGust24hKmh(request.windGust24hKmh());
        reading.setWindAvg1hKmh(request.windAvg1hKmh());
        reading.setWindDirectionDeg(request.windDirectionDeg());
    }

    private ReadingResponse toResponse(Reading reading) {
        return new ReadingResponse(
                reading.getId(),
                reading.getStation().getId(),
                reading.getRecordedAt(),
                reading.getTemperatureC(),
                reading.getHumidityPct(),
                reading.getPressureHpa(),
                reading.getRainLastHourMm(),
                reading.getRain24hMm(),
                reading.getWindSpeedKmh(),
                reading.getWindGust24hKmh(),
                reading.getWindAvg1hKmh(),
                reading.getWindDirectionDeg()
        );
    }
}
