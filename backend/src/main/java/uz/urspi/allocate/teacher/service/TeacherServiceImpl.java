package uz.urspi.allocate.teacher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.security.AccessScope;
import uz.urspi.allocate.teacher.dto.TeacherRequest;
import uz.urspi.allocate.teacher.entity.Teacher;
import uz.urspi.allocate.teacher.repository.TeacherRepository;
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.teacher.response.TeacherResponse;
import uz.urspi.allocate.teacher.response.TeacherWorkloadAllocationResponse;
import uz.urspi.allocate.teacher.response.TeacherWorkloadSummaryResponse;
import uz.urspi.allocate.workload.entity.WorkloadAllocation;
import uz.urspi.allocate.workload.repository.WorkloadAllocationRepository;
import uz.urspi.allocate.workload.response.AllocatedGroupResponse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private static final int LIGHT_LOAD_MAX = 300;
    private static final int NORMAL_LOAD_MAX = 600;

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final WorkloadAllocationRepository allocationRepository;

    @Override
    @Auditable(entity = "Teacher", action = AuditAction.CREATE)
    public TeacherResponse create(TeacherRequest request) {
        Teacher teacher = Teacher.builder()
                .fullName(request.getName())
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .thirdName(request.getThirdName())
                .employeeIdNumber(request.getEmployeeIdNumber())
                .department(resolveDepartment(request.getDepartmentId()))
                .staffPositionName(request.getStaffPositionName())
                .staffPositionCode(request.getStaffPositionCode())
                .stavka(request.getStavka())
                .build();
        teacher.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(teacherRepository.save(teacher));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherResponse> findAll(Long facultyId, Long departmentId) {
        AccessScope scope = AccessScope.ofCurrentUser();
        Long effectiveFacultyId = scope.resolveFacultyId(facultyId);
        Long effectiveDepartmentId = scope.resolveDepartmentId(departmentId);

        List<Teacher> teachers;
        if (!scope.isUnrestricted()
                && ((effectiveDepartmentId != null && effectiveDepartmentId < 0)
                || (effectiveFacultyId != null && effectiveFacultyId < 0
                && effectiveDepartmentId == null))) {
            teachers = List.of();
        } else if (effectiveDepartmentId != null) {
            teachers = teacherRepository.findByDepartment_Id(effectiveDepartmentId);
        } else if (effectiveFacultyId != null) {
            teachers = teacherRepository.findByDepartment_Faculty_Id(effectiveFacultyId);
        } else {
            teachers = teacherRepository.findAll();
        }
        return teachers.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherWorkloadSummaryResponse> workloadSummary(Long facultyId, Long departmentId) {
        return findAll(facultyId, departmentId).stream()
                .map(t -> {
                    List<WorkloadAllocation> allocations = allocationRepository.findByTeacher_Id(t.getId());
                    int lecture = 0, practical = 0, lab = 0, seminar = 0, rating = 0;
                    int groupCount = 0, studentCount = 0;
                    for (WorkloadAllocation a : allocations) {
                        lecture += orZero(a.getLectureHours());
                        practical += orZero(a.getPracticalHours());
                        lab += orZero(a.getLabHours());
                        seminar += orZero(a.getSeminarHours());
                        rating += orZero(a.getRatingHours());
                        if (a.getGroups() != null && !a.getGroups().isEmpty()) {
                            groupCount += a.getGroups().size();
                            studentCount += a.getGroups().stream()
                                    .mapToInt(g -> orZero(g.getStudentCount()))
                                    .sum();
                        } else if (a.getSubject() != null) {
                            groupCount += orZero(a.getSubject().getGroupCount());
                            studentCount += orZero(a.getSubject().getStudentCount());
                        }
                    }
                    int total = lecture + practical + lab + seminar + rating;
                    int independent = allocations.stream()
                            .mapToInt(a -> a.getSubject() != null ? orZero(a.getSubject().getIndependentStudyHours()) : 0)
                            .sum();
                    return TeacherWorkloadSummaryResponse.builder()
                            .id(t.getId())
                            .name(t.getName())
                            .fullName(t.getFullName() != null ? t.getFullName() : t.getName())
                            .departmentId(t.getDepartmentId())
                            .departmentName(t.getDepartmentName())
                            .facultyId(t.getFacultyId())
                            .facultyName(t.getFacultyName())
                            .stavka(t.getStavka())
                            .subjectCount(allocations.size())
                            .lectureHours(lecture)
                            .practicalHours(practical)
                            .labHours(lab)
                            .seminarHours(seminar)
                            .ratingHours(rating)
                            .independentHours(independent)
                            .totalHours(total)
                            .groupCount(groupCount)
                            .studentCount(studentCount)
                            .loadLabel(loadLabel(total))
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherWorkloadAllocationResponse> workloadAllocations(Long teacherId) {
        getOrThrow(teacherId);
        return allocationRepository.findByTeacher_Id(teacherId).stream()
                .map(this::toAllocationResponse)
                .toList();
    }

    private TeacherWorkloadAllocationResponse toAllocationResponse(WorkloadAllocation a) {
        Subject subject = a.getSubject();
        int lecture = orZero(a.getLectureHours());
        int practical = orZero(a.getPracticalHours());
        int lab = orZero(a.getLabHours());
        int seminar = orZero(a.getSeminarHours());
        int rating = orZero(a.getRatingHours());
        int totalSubjectHours = subject != null ? orZero(subject.getTotalSubjectHours()) : 0;
        if (totalSubjectHours <= 0 && subject != null) {
            totalSubjectHours = orZero(subject.getTotalHours());
        }
        double credit = totalSubjectHours > 0 ? Math.round((totalSubjectHours / 30.0) * 100.0) / 100.0 : 0;
        List<AllocatedGroupResponse> groups = a.getGroups() == null ? List.of() : a.getGroups().stream()
                .sorted((x, y) -> String.valueOf(x.getName()).compareToIgnoreCase(String.valueOf(y.getName())))
                .map(g -> AllocatedGroupResponse.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .studentCount(orZero(g.getStudentCount()))
                        .build())
                .toList();
        int allocationStudentCount = groups.isEmpty()
                ? (subject != null ? orZero(subject.getStudentCount()) : 0)
                : groups.stream().mapToInt(g -> orZero(g.getStudentCount())).sum();
        return TeacherWorkloadAllocationResponse.builder()
                .allocationId(a.getId())
                .subjectId(subject != null ? subject.getId() : null)
                .subjectCode(subject != null ? subject.getCode() : null)
                .subjectName(subject != null ? subject.getName() : null)
                .departmentName(subject != null && subject.getDepartment() != null
                        ? subject.getDepartment().getName() : null)
                .semester(subject != null ? subject.getSemester() : null)
                .courseYear(subject != null ? subject.getCourseYear() : null)
                .lectureHours(lecture)
                .practicalHours(practical)
                .labHours(lab)
                .seminarHours(seminar)
                .ratingHours(rating)
                .totalHours(lecture + practical + lab + seminar + rating)
                .independentHours(subject != null ? orZero(subject.getIndependentStudyHours()) : 0)
                .totalSubjectHours(totalSubjectHours)
                .credit(credit)
                .groupCount(groups.isEmpty()
                        ? (subject != null ? orZero(subject.getGroupCount()) : 0)
                        : groups.size())
                .studentCount(allocationStudentCount)
                .groups(groups)
                .build();
    }

    private static int orZero(Integer v) {
        return v == null ? 0 : v;
    }

    private static String loadLabel(int total) {
        if (total <= LIGHT_LOAD_MAX) return "Kam yuklangan";
        if (total <= NORMAL_LOAD_MAX) return "O'rtacha";
        return "Ko'p yuklangan";
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Auditable(entity = "Teacher", action = AuditAction.UPDATE)
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = getOrThrow(id);
        teacher.setFullName(request.getName());
        if (request.getFirstName() != null) {
            teacher.setFirstName(request.getFirstName());
        }
        if (request.getSecondName() != null) {
            teacher.setSecondName(request.getSecondName());
        }
        if (request.getThirdName() != null) {
            teacher.setThirdName(request.getThirdName());
        }
        if (request.getEmployeeIdNumber() != null) {
            teacher.setEmployeeIdNumber(request.getEmployeeIdNumber());
        }
        if (request.getDepartmentId() != null) {
            teacher.setDepartment(resolveDepartment(request.getDepartmentId()));
        }
        if (request.getStaffPositionName() != null) {
            teacher.setStaffPositionName(request.getStaffPositionName());
        }
        if (request.getStaffPositionCode() != null) {
            teacher.setStaffPositionCode(request.getStaffPositionCode());
        }
        teacher.setStavka(request.getStavka());
        return toResponse(teacherRepository.save(teacher));
    }

    @Override
    @Auditable(entity = "Teacher", action = AuditAction.DELETE)
    public void delete(Long id) {
        Teacher teacher = getOrThrow(id);
        teacher.softDelete();
        teacherRepository.save(teacher);
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", departmentId));
    }

    private Teacher getOrThrow(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", id));
    }

    private TeacherResponse toResponse(Teacher teacher) {
        String displayName = StringUtils.hasText(teacher.getFullName())
                ? teacher.getFullName()
                : Stream.of(teacher.getSecondName(), teacher.getFirstName(), teacher.getThirdName())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
        return TeacherResponse.builder()
                .id(teacher.getId())
                .name(displayName)
                .firstName(teacher.getFirstName())
                .secondName(teacher.getSecondName())
                .thirdName(teacher.getThirdName())
                .fullName(teacher.getFullName())
                .shortName(teacher.getShortName())
                .employeeIdNumber(teacher.getEmployeeIdNumber())
                .status(teacher.getStatus())
                .hemisId(teacher.getHemisId())
                .departmentId(teacher.getDepartment() != null ? teacher.getDepartment().getId() : null)
                .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                .facultyId(teacher.getDepartment() != null && teacher.getDepartment().getFaculty() != null
                        ? teacher.getDepartment().getFaculty().getId() : null)
                .facultyName(teacher.getDepartment() != null && teacher.getDepartment().getFaculty() != null
                        ? teacher.getDepartment().getFaculty().getName() : null)
                .staffPositionName(teacher.getStaffPositionName())
                .staffPositionCode(teacher.getStaffPositionCode())
                .employeeTypeName(teacher.getEmployeeTypeName())
                .academicRankName(teacher.getAcademicRankName())
                .academicDegreeName(teacher.getAcademicDegreeName())
                .image(teacher.getImage())
                .stavka(teacher.getStavka())
                .build();
    }
}
