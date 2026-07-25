package uz.urspi.allocate.academicyear.controller;

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
import uz.urspi.allocate.academicyear.dto.AcademicYearRequest;
import uz.urspi.allocate.academicyear.response.AcademicYearResponse;
import uz.urspi.allocate.academicyear.service.AcademicYearService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academic-years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_CREATE')")
    public ResponseEntity<AcademicYearResponse> create(@Valid @RequestBody AcademicYearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicYearService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_VIEW')")
    public ResponseEntity<List<AcademicYearResponse>> findAll() {
        return ResponseEntity.ok(academicYearService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_VIEW')")
    public ResponseEntity<AcademicYearResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(academicYearService.findById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_EDIT')")
    public ResponseEntity<AcademicYearResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AcademicYearRequest request
    ) {
        return ResponseEntity.ok(academicYearService.update(id, request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        academicYearService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
