package com.estacao.meteorologica.controller;

import com.estacao.meteorologica.dto.ReadingRequest;
import com.estacao.meteorologica.dto.ReadingResponse;
import com.estacao.meteorologica.service.ReadingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
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
public class ReadingController {

    private final ReadingService readingService;

    /** Espelha o hook useLatestReading(stationId). */
    @GetMapping("/stations/{stationId}/readings/latest")
    public ResponseEntity<ReadingResponse> latest(@PathVariable UUID stationId) {
        return ResponseEntity.ok(readingService.findLatestByStation(stationId));
    }

    /** Espelha o hook useReadingsHistory(stationId, hours=24). */
    @GetMapping("/stations/{stationId}/readings/history")
    public ResponseEntity<List<ReadingResponse>> history(
            @PathVariable UUID stationId,
            @RequestParam(defaultValue = "24") @Positive(message = "hours deve ser positivo.") int hours) {
        return ResponseEntity.ok(readingService.findHistoryByStation(stationId, hours));
    }

    /** Listagem completa (mais recentes primeiro) das leituras de uma estação. */
    @GetMapping("/stations/{stationId}/readings")
    public ResponseEntity<List<ReadingResponse>> listByStation(@PathVariable UUID stationId) {
        return ResponseEntity.ok(readingService.findAllByStation(stationId));
    }

    @PostMapping("/stations/{stationId}/readings")
    public ResponseEntity<ReadingResponse> create(
            @PathVariable UUID stationId, @Valid @RequestBody ReadingRequest request) {
        ReadingResponse created = readingService.create(stationId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/readings/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** Listagem paginada entre todas as estações (readings tende a crescer bastante). */
    @GetMapping("/readings")
    public ResponseEntity<PagedModel<ReadingResponse>> listAll(
            @PageableDefault(size = 50, sort = "recordedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(readingService.findAll(pageable)));
    }

    @GetMapping("/readings/{id}")
    public ResponseEntity<ReadingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(readingService.findById(id));
    }

    @PutMapping("/readings/{id}")
    public ResponseEntity<ReadingResponse> update(
            @PathVariable Long id, @Valid @RequestBody ReadingRequest request) {
        return ResponseEntity.ok(readingService.update(id, request));
    }

    @DeleteMapping("/readings/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        readingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
