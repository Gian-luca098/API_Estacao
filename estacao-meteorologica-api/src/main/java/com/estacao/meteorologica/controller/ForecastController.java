package com.estacao.meteorologica.controller;

import com.estacao.meteorologica.dto.ForecastRequest;
import com.estacao.meteorologica.dto.ForecastResponse;
import com.estacao.meteorologica.service.ForecastService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ForecastController {

    private final ForecastService forecastService;

    /**
     * Sem "limit", devolve todas as previsões da estação. Com "?limit=6", espelha o hook
     * useForecast(stationId, limit=6) do front-end.
     */
    @GetMapping("/stations/{stationId}/forecasts")
    public ResponseEntity<List<ForecastResponse>> listByStation(
            @PathVariable UUID stationId,
            @RequestParam(required = false) @Positive(message = "limit deve ser positivo.") Integer limit) {
        return ResponseEntity.ok(forecastService.findByStation(stationId, limit));
    }

    @PostMapping("/stations/{stationId}/forecasts")
    public ResponseEntity<ForecastResponse> create(
            @PathVariable UUID stationId, @Valid @RequestBody ForecastRequest request) {
        ForecastResponse created = forecastService.create(stationId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/forecasts/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/forecasts/{id}")
    public ResponseEntity<ForecastResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(forecastService.findById(id));
    }

    @PutMapping("/forecasts/{id}")
    public ResponseEntity<ForecastResponse> update(
            @PathVariable Long id, @Valid @RequestBody ForecastRequest request) {
        return ResponseEntity.ok(forecastService.update(id, request));
    }

    @DeleteMapping("/forecasts/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        forecastService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
