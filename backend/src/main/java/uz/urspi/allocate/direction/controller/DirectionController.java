package uz.urspi.allocate.direction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.urspi.allocate.direction.dto.DirectionRequest;
import uz.urspi.allocate.direction.response.DirectionResponse;
import uz.urspi.allocate.direction.service.DirectionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directions")
public class DirectionController {

    private final DirectionService directionService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('DIRECTION_CREATE')")
    public ResponseEntity<DirectionResponse> create(@Valid @RequestBody DirectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(directionService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DIRECTION_VIEW')")
    public ResponseEntity<List<DirectionResponse>> findAll() {
        return ResponseEntity.ok(directionService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DIRECTION_VIEW')")
    public ResponseEntity<DirectionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(directionService.findById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('DIRECTION_EDIT')")
    public ResponseEntity<DirectionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DirectionRequest request
    ) {
        return ResponseEntity.ok(directionService.update(id, request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('DIRECTION_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        directionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
