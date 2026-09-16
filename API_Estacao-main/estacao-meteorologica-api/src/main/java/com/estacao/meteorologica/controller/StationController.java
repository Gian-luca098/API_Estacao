package com.estacao.meteorologica.controller;

import com.estacao.meteorologica.dto.StationRequest;
import com.estacao.meteorologica.dto.StationResponse;
import com.estacao.meteorologica.service.StationService;
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
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public ResponseEntity<List<StationResponse>> listAll() {
        return ResponseEntity.ok(stationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(stationService.findById(id));
    }

    /** Espelha o hook useStation(stationCode) do front-end. */
    @GetMapping("/code/{code}")
    public ResponseEntity<StationResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(stationService.findByCode(code));
    }

    @PostMapping
    public ResponseEntity<StationResponse> create(@Valid @RequestBody StationRequest request) {
        StationResponse created = stationService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StationResponse> update(
            @PathVariable UUID id, @Valid @RequestBody StationRequest request) {
        return ResponseEntity.ok(stationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        stationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
