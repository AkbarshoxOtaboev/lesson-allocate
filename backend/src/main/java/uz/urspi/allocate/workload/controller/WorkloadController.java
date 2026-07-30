package uz.urspi.allocate.workload.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.workload.dto.WorkloadAllocateRequest;
import uz.urspi.allocate.workload.response.DashboardHoursResponse;
import uz.urspi.allocate.workload.response.TeacherLoadResponse;
import uz.urspi.allocate.workload.response.WorkloadDetailResponse;
import uz.urspi.allocate.workload.response.WorkloadRowResponse;
import uz.urspi.allocate.workload.service.WorkloadService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workloads")
public class WorkloadController {

    private final WorkloadService workloadService;

    @GetMapping
    @PreAuthorize("hasAuthority('WORKLOAD_VIEW') or hasAuthority('SUBJECT_VIEW')")
    public ResponseEntity<List<WorkloadRowResponse>> list(
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Semester semester,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer courseYear
    ) {
        return ResponseEntity.ok(workloadService.list(facultyId, departmentId, semester, status, courseYear));
    }

    @GetMapping("/dashboard-hours")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardHoursResponse> dashboardHours() {
        return ResponseEntity.ok(workloadService.dashboardHours());
    }

    @GetMapping("/{subjectId}")
    @PreAuthorize("hasAuthority('WORKLOAD_VIEW') or hasAuthority('SUBJECT_VIEW')")
    public ResponseEntity<WorkloadDetailResponse> detail(@PathVariable Long subjectId) {
        return ResponseEntity.ok(workloadService.detail(subjectId));
    }

    @GetMapping("/{subjectId}/teachers")
    @PreAuthorize("hasAuthority('WORKLOAD_VIEW') or hasAuthority('SUBJECT_VIEW')")
    public ResponseEntity<List<TeacherLoadResponse>> teachers(@PathVariable Long subjectId) {
        return ResponseEntity.ok(workloadService.teachersForSubject(subjectId));
    }

    @PostMapping("/allocate")
    @PreAuthorize("hasAuthority('WORKLOAD_CREATE') or hasAuthority('WORKLOAD_EDIT') or hasAuthority('SUBJECT_EDIT')")
    public ResponseEntity<WorkloadDetailResponse> allocate(@Valid @RequestBody WorkloadAllocateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workloadService.allocate(request));
    }
}
