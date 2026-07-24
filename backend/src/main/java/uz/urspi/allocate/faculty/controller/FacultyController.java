package uz.urspi.allocate.faculty.controller;

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
import uz.urspi.allocate.faculty.dto.NameRequest;
import uz.urspi.allocate.faculty.response.FacultyResponse;
import uz.urspi.allocate.faculty.service.FacultyService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/faculties")
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('FACULTY_CREATE')")
    public ResponseEntity<FacultyResponse> create(@Valid @RequestBody NameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facultyService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FACULTY_VIEW')")
    public ResponseEntity<List<FacultyResponse>> findAll() {
        return ResponseEntity.ok(facultyService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FACULTY_VIEW')")
    public ResponseEntity<FacultyResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.findById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('FACULTY_EDIT')")
    public ResponseEntity<FacultyResponse> update(@PathVariable Long id, @Valid @RequestBody NameRequest request) {
        return ResponseEntity.ok(facultyService.update(id, request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('FACULTY_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facultyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
