package uz.urspi.allocate.teacher.controller;

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
import uz.urspi.allocate.teacher.dto.TeacherRequest;
import uz.urspi.allocate.teacher.response.TeacherResponse;
import uz.urspi.allocate.teacher.response.TeacherWorkloadSummaryResponse;
import uz.urspi.allocate.teacher.service.TeacherService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('TEACHER_CREATE')")
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TEACHER_VIEW')")
    public ResponseEntity<List<TeacherResponse>> findAll(
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long departmentId
    ) {
        return ResponseEntity.ok(teacherService.findAll(facultyId, departmentId));
    }

    @GetMapping("/workload-summary")
    @PreAuthorize("hasAuthority('TEACHER_VIEW')")
    public ResponseEntity<List<TeacherWorkloadSummaryResponse>> workloadSummary(
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long departmentId
    ) {
        return ResponseEntity.ok(teacherService.workloadSummary(facultyId, departmentId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER_VIEW')")
    public ResponseEntity<TeacherResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.findById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('TEACHER_EDIT')")
    public ResponseEntity<TeacherResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TeacherRequest request
    ) {
        return ResponseEntity.ok(teacherService.update(id, request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('TEACHER_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
