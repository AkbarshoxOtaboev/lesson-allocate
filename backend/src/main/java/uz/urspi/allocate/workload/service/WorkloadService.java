package uz.urspi.allocate.workload.service;

import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.workload.dto.WorkloadAllocateRequest;
import uz.urspi.allocate.workload.response.DashboardHoursResponse;
import uz.urspi.allocate.workload.response.TeacherLoadResponse;
import uz.urspi.allocate.workload.response.WorkloadDetailResponse;
import uz.urspi.allocate.workload.response.WorkloadRowResponse;

import java.util.List;

public interface WorkloadService {

    List<WorkloadRowResponse> list(Long facultyId, Long departmentId, Semester semester, String status);

    WorkloadDetailResponse detail(Long subjectId);

    List<TeacherLoadResponse> teachersForSubject(Long subjectId);

    WorkloadDetailResponse allocate(WorkloadAllocateRequest request);

    DashboardHoursResponse dashboardHours();
}
