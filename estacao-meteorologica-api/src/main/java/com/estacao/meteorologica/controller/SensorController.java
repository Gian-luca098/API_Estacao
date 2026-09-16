package com.estacao.meteorologica.controller;

import com.estacao.meteorologica.dto.SensorRequest;
import com.estacao.meteorologica.dto.SensorResponse;
import com.estacao.meteorologica.service.SensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    /** Espelha o hook useSensors(stationId). */
    @GetMapping("/stations/{stationId}/sensors")
    public ResponseEntity<List<SensorResponse>> listByStation(@PathVariable UUID stationId) {
        return ResponseEntity.ok(sensorService.findByStation(stationId));
    }

    @PostMapping("/stations/{stationId}/sensors")
    public ResponseEntity<SensorResponse> create(
            @PathVariable UUID stationId, @Valid @RequestBody SensorRequest request) {
        SensorResponse created = sensorService.create(stationId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/sensors/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/sensors/{id}")
    public ResponseEntity<SensorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sensorService.findById(id));
    }

    @PutMapping("/sensors/{id}")
    public ResponseEntity<SensorResponse> update(
            @PathVariable Long id, @Valid @RequestBody SensorRequest request) {
        return ResponseEntity.ok(sensorService.update(id, request));
    }

    @DeleteMapping("/sensors/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sensorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
