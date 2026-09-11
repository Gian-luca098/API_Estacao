package com.estacao.meteorologica.service;

import com.estacao.meteorologica.dto.ForecastRequest;
import com.estacao.meteorologica.dto.ForecastResponse;
import com.estacao.meteorologica.exception.ResourceNotFoundException;
import com.estacao.meteorologica.model.Forecast;
import com.estacao.meteorologica.model.Station;
import com.estacao.meteorologica.repository.ForecastRepository;
import com.estacao.meteorologica.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForecastService {

    private final ForecastRepository forecastRepository;
    private final StationRepository stationRepository;

    /**
     * Lista as previsões de uma estação em ordem cronológica. Quando {@code limit} é
     * informado, espelha o hook useForecast(stationId, limit=6); quando omitido (null),
     * devolve a coleção completa, comportamento padrão esperado de um GET de coleção.
     */
    public List<ForecastResponse> findByStation(UUID stationId, Integer limit) {
        ensureStationExists(stationId);
        List<Forecast> forecasts = (limit != null)
                ? forecastRepository.findByStationIdOrderByForecastForAsc(stationId, PageRequest.of(0, limit))
                : forecastRepository.findByStationIdOrderByForecastForAsc(stationId);
        return forecasts.stream().map(this::toResponse).toList();
    }

    public ForecastResponse findById(Long id) {
        return toResponse(getForecastOrThrow(id));
    }

    @Transactional
    public ForecastResponse create(UUID stationId, ForecastRequest request) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma estação encontrada com o id '" + stationId + "'."));
        Forecast forecast = new Forecast();
        forecast.setStation(station);
        applyRequest(forecast, request);
        return toResponse(forecastRepository.save(forecast));
    }

    @Transactional
    public ForecastResponse update(Long id, ForecastRequest request) {
        Forecast forecast = getForecastOrThrow(id);
        if (request.stationId() != null && !request.stationId().equals(forecast.getStation().getId())) {
            Station station = stationRepository.findById(request.stationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Nenhuma estação encontrada com o id '" + request.stationId() + "'."));
            forecast.setStation(station);
        }
        applyRequest(forecast, request);
        return toResponse(forecastRepository.save(forecast));
    }

    @Transactional
    public void delete(Long id) {
        forecastRepository.delete(getForecastOrThrow(id));
    }

    private Forecast getForecastOrThrow(Long id) {
        return forecastRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma previsão encontrada com o id '" + id + "'."));
    }

    private void ensureStationExists(UUID stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("Nenhuma estação encontrada com o id '" + stationId + "'.");
        }
    }

    private void applyRequest(Forecast forecast, ForecastRequest request) {
        forecast.setForecastFor(request.forecastFor());
        forecast.setLabel(request.label());
        forecast.setTemperatureC(request.temperatureC());
        forecast.setRainChancePct(request.rainChancePct());
        forecast.setIcon(request.icon());
    }

    private ForecastResponse toResponse(Forecast forecast) {
        return new ForecastResponse(
                forecast.getId(),
                forecast.getStation().getId(),
                forecast.getForecastFor(),
                forecast.getLabel(),
                forecast.getTemperatureC(),
                forecast.getRainChancePct(),
                forecast.getIcon()
        );
    }
}
