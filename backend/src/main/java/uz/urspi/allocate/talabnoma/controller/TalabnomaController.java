package uz.urspi.allocate.talabnoma.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.urspi.allocate.talabnoma.dto.TalabnomaRejectRequest;
import uz.urspi.allocate.talabnoma.dto.TalabnomaRequest;
import uz.urspi.allocate.talabnoma.enums.TalabnomaStatus;
import uz.urspi.allocate.talabnoma.response.TalabnomaResponse;
import uz.urspi.allocate.talabnoma.response.TalabnomaStatsResponse;
import uz.urspi.allocate.talabnoma.service.TalabnomaService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/talabnomalar")
public class TalabnomaController {

    private final TalabnomaService talabnomaService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('TALABNOMA_CREATE')")
    public ResponseEntity<TalabnomaResponse> create(@Valid @RequestBody TalabnomaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(talabnomaService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TALABNOMA_CREATE')")
    public ResponseEntity<TalabnomaResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TalabnomaRequest request
    ) {
        return ResponseEntity.ok(talabnomaService.update(id, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TALABNOMA_VIEW')")
    public ResponseEntity<List<TalabnomaResponse>> findAll(
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) TalabnomaStatus status
    ) {
        return ResponseEntity.ok(talabnomaService.findAll(facultyId, departmentId, status));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('TALABNOMA_VIEW')")
    public ResponseEntity<TalabnomaStatsResponse> stats(
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long departmentId
    ) {
        return ResponseEntity.ok(talabnomaService.stats(facultyId, departmentId));
    }

    @GetMapping("/new-count")
    @PreAuthorize("hasAuthority('TALABNOMA_VIEW')")
    public ResponseEntity<Map<String, Long>> newCount() {
        return ResponseEntity.ok(Map.of("count", talabnomaService.countNewForCurrentUser()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TALABNOMA_VIEW')")
    public ResponseEntity<TalabnomaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(talabnomaService.findById(id));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('TALABNOMA_EDIT')")
    public ResponseEntity<TalabnomaResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(talabnomaService.accept(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('TALABNOMA_EDIT')")
    public ResponseEntity<TalabnomaResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) TalabnomaRejectRequest request
    ) {
        return ResponseEntity.ok(talabnomaService.reject(id, request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('TALABNOMA_DELETE', 'TALABNOMA_CREATE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        talabnomaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
