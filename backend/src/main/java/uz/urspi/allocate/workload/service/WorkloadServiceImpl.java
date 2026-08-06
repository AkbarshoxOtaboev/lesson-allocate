package uz.urspi.allocate.workload.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.faculty.repository.FacultyRepository;
import uz.urspi.allocate.group.entity.Group;
import uz.urspi.allocate.group.repository.GroupRepository;
import uz.urspi.allocate.security.AccessScope;
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.subject.repository.SubjectRepository;
import uz.urspi.allocate.teacher.entity.Teacher;
import uz.urspi.allocate.teacher.repository.TeacherRepository;
import uz.urspi.allocate.talabnoma.entity.Talabnoma;
import uz.urspi.allocate.talabnoma.repository.TalabnomaRepository;
import uz.urspi.allocate.talabnoma.service.TalabnomaService;
import uz.urspi.allocate.workload.dto.WorkloadAllocateRequest;
import uz.urspi.allocate.workload.entity.WorkloadAllocation;
import uz.urspi.allocate.workload.enums.AllocationStatus;
import uz.urspi.allocate.workload.repository.WorkloadAllocationRepository;
import uz.urspi.allocate.workload.response.AllocatedGroupResponse;
import uz.urspi.allocate.workload.response.DashboardHoursResponse;
import uz.urspi.allocate.workload.response.HoursByGroupResponse;
import uz.urspi.allocate.workload.response.TeacherLoadResponse;
import uz.urspi.allocate.workload.response.WorkloadDetailResponse;
import uz.urspi.allocate.workload.response.WorkloadRowResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkloadServiceImpl implements WorkloadService {

    private static final int LIGHT_LOAD_MAX = 300;
    private static final int NORMAL_LOAD_MAX = 600;

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final WorkloadAllocationRepository allocationRepository;
    private final GroupRepository groupRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final TalabnomaService talabnomaService;
    private final TalabnomaRepository talabnomaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WorkloadRowResponse> list(Long facultyId, Long departmentId, Semester semester, String status, Integer courseYear) {
        AccessScope scope = AccessScope.ofCurrentUser();
        Long effectiveFacultyId = scope.resolveFacultyId(facultyId);
        Long effectiveDepartmentId = scope.resolveDepartmentId(departmentId);

        List<Subject> subjects;
        if (!scope.isUnrestricted()
                && ((effectiveDepartmentId != null && effectiveDepartmentId < 0)
                || (effectiveFacultyId != null && effectiveFacultyId < 0
                && effectiveDepartmentId == null))) {
            subjects = List.of();
        } else if (effectiveDepartmentId != null) {
            subjects = semester == null
                    ? subjectRepository.findByDepartment_Id(effectiveDepartmentId)
                    : subjectRepository.findByDepartment_IdAndSemester(effectiveDepartmentId, semester);
        } else if (effectiveFacultyId != null) {
            subjects = semester == null
                    ? subjectRepository.findByDepartment_Faculty_Id(effectiveFacultyId)
                    : subjectRepository.findByDepartment_Faculty_IdAndSemester(effectiveFacultyId, semester);
        } else if (semester != null) {
            subjects = subjectRepository.findBySemester(semester);
        } else {
            subjects = subjectRepository.findAll();
        }

        if (courseYear != null) {
            int year = Math.max(1, Math.min(5, courseYear));
            subjects = subjects.stream()
                    .filter(s -> {
                        int cy = s.getCourseYear() != null ? s.getCourseYear() : 1;
                        return cy == year;
                    })
                    .toList();
        }

        AllocationStatus filterStatus = parseStatus(status);
        return subjects.stream()
                .map(this::toRow)
                .filter(row -> filterStatus == null || row.getAllocationStatus() == filterStatus)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkloadDetailResponse detail(Long subjectId) {
        return toDetail(getSubjectOrThrow(subjectId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherLoadResponse> teachersForSubject(Long subjectId) {
        Subject subject = getSubjectOrThrow(subjectId);
        Long departmentId = subject.getDepartment() != null ? subject.getDepartment().getId() : null;
        if (departmentId == null) {
            return List.of();
        }

        Map<Long, WorkloadAllocation> existingByTeacher = allocationRepository.findBySubject_Id(subjectId)
                .stream()
                .collect(Collectors.toMap(a -> a.getTeacher().getId(), a -> a, (a, b) -> a));

        return teacherRepository.findByDepartment_Id(departmentId).stream()
                .map(teacher -> {
                    long total = allocationRepository.sumHoursByTeacherId(teacher.getId());
                    WorkloadAllocation existing = existingByTeacher.get(teacher.getId());
                    return TeacherLoadResponse.builder()
                            .id(teacher.getId())
                            .name(teacherDisplayName(teacher))
                            .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                            .staffPositionName(teacher.getStaffPositionName())
                            .employmentStaffName(existing != null && StringUtils.hasText(existing.getEmploymentStaffName())
                                    ? existing.getEmploymentStaffName()
                                    : teacher.getEmploymentStaffName())
                            .existingWorkloadRate(existing != null && existing.getWorkloadRate() != null
                                    ? existing.getWorkloadRate()
                                    : teacher.getStavka())
                            .totalAssignedHours((int) total)
                            .loadLabel(loadLabel((int) total))
                            .existingLectureHours(existing != null ? orZero(existing.getLectureHours()) : 0)
                            .existingSeminarHours(existing != null ? orZero(existing.getSeminarHours()) : 0)
                            .existingPracticalHours(existing != null ? orZero(existing.getPracticalHours()) : 0)
                            .existingLabHours(existing != null ? orZero(existing.getLabHours()) : 0)
                            .existingRatingHours(existing != null ? orZero(existing.getRatingHours()) : 0)
                            .existingGroups(existing != null ? toGroupResponses(existing.getGroups()) : List.of())
                            .build();
                })
                .toList();
    }

    @Override
    @Auditable(entity = "Workload", action = AuditAction.UPDATE)
    public WorkloadDetailResponse allocate(WorkloadAllocateRequest request) {
        Subject subject = getSubjectOrThrow(request.getSubjectId());
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", request.getTeacherId()));

        if (subject.getDepartment() == null
                || teacher.getDepartment() == null
                || !subject.getDepartment().getId().equals(teacher.getDepartment().getId())) {
            throw new BadRequestException("O'qituvchi ushbu fanning kafedrasiga tegishli emas");
        }

        int lecture = orZero(request.getLectureHours());
        int seminar = orZero(request.getSeminarHours());
        int practical = orZero(request.getPracticalHours());
        int lab = orZero(request.getLabHours());
        int rating = orZero(request.getRatingHours());

        if (lecture + seminar + practical + lab + rating <= 0) {
            throw new BadRequestException("Kamida bitta soat taqsimlanishi kerak");
        }

        WorkloadAllocation allocation = allocationRepository
                .findBySubject_IdAndTeacher_Id(subject.getId(), teacher.getId())
                .orElse(null);

        int prevLecture = allocation != null ? orZero(allocation.getLectureHours()) : 0;
        int prevSeminar = allocation != null ? orZero(allocation.getSeminarHours()) : 0;
        int prevPractical = allocation != null ? orZero(allocation.getPracticalHours()) : 0;
        int prevLab = allocation != null ? orZero(allocation.getLabHours()) : 0;
        int prevRating = allocation != null ? orZero(allocation.getRatingHours()) : 0;

        int[] sums = allocatedSums(subject.getId());
        validateBucket("Ma'ruza", lecture, orZero(subject.getLectureHours()), sums[0] - prevLecture);
        validateBucket("Seminar", seminar, orZero(subject.getSeminarHours()), sums[1] - prevSeminar);
        validateBucket("Amaliy", practical, orZero(subject.getPracticalHours()), sums[2] - prevPractical);
        validateBucket("Laboratoriya", lab, orZero(subject.getLabHours()), sums[3] - prevLab);
        validateBucket("Reyting", rating, orZero(subject.getRatingHours()), sums[4] - prevRating);

        if (allocation == null) {
            allocation = WorkloadAllocation.builder()
                    .subject(subject)
                    .teacher(teacher)
                    .build();
            allocation.setCreatedUsername(SecurityUtils.getCurrentUsername());
        }
        allocation.setLectureHours(lecture);
        allocation.setSeminarHours(seminar);
        allocation.setPracticalHours(practical);
        allocation.setLabHours(lab);
        allocation.setRatingHours(rating);
        allocation.setEmploymentStaffName(
                StringUtils.hasText(request.getEmploymentStaffName())
                        ? request.getEmploymentStaffName().trim()
                        : teacher.getEmploymentStaffName()
        );
        allocation.setWorkloadRate(request.getWorkloadRate());
        allocation.setGroups(resolveGroups(request.getGroupIds(), subject.getDepartment()));
        allocationRepository.save(allocation);

        talabnomaService.refreshStatusForSubject(subject.getId());

        return toDetail(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardHoursResponse dashboardHours() {
        AccessScope scope = AccessScope.ofCurrentUser();
        Long facultyId = scope.resolveFacultyId(null);
        Long departmentId = scope.resolveDepartmentId(null);

        List<Subject> subjects;
        if (!scope.isUnrestricted()
                && ((departmentId != null && departmentId < 0)
                || (facultyId != null && facultyId < 0 && departmentId == null))) {
            subjects = List.of();
        } else if (departmentId != null) {
            subjects = subjectRepository.findByDepartment_Id(departmentId);
        } else if (facultyId != null) {
            subjects = subjectRepository.findByDepartment_Faculty_Id(facultyId);
        } else {
            subjects = subjectRepository.findAll();
        }

        int total = 0;
        int allocated = 0;
        for (Subject subject : subjects) {
            int auditorium = auditoriumTotal(subject);
            total += auditorium;
            allocated += Math.min(auditorium, (int) allocationRepository.sumHoursBySubjectId(subject.getId()));
        }

        List<HoursByGroupResponse> byFaculty = buildFacultyStats(scope, facultyId, departmentId);
        List<HoursByGroupResponse> byDepartment = buildDepartmentStats(scope, facultyId, departmentId);

        return DashboardHoursResponse.builder()
                .totalHours(total)
                .allocatedHours(allocated)
                .unallocatedHours(Math.max(0, total - allocated))
                .byFaculty(byFaculty)
                .byDepartment(byDepartment)
                .build();
    }

    private List<HoursByGroupResponse> buildFacultyStats(AccessScope scope, Long facultyId, Long departmentId) {
        List<Faculty> faculties;
        if (departmentId != null && departmentId > 0) {
            Department dep = departmentRepository.findById(departmentId).orElse(null);
            if (dep != null && dep.getFaculty() != null) {
                faculties = List.of(dep.getFaculty());
            } else {
                faculties = List.of();
            }
        } else if (facultyId != null && facultyId > 0) {
            faculties = facultyRepository.findById(facultyId).map(List::of).orElse(List.of());
        } else if (!scope.isUnrestricted() && facultyId != null && facultyId < 0) {
            faculties = List.of();
        } else {
            faculties = facultyRepository.findAll();
        }

        return faculties.stream()
                .map(faculty -> {
                    List<Subject> subjects = subjectRepository.findByDepartment_Faculty_Id(faculty.getId());
                    int[] hours = sumHours(subjects);
                    return HoursByGroupResponse.builder()
                            .id(faculty.getId())
                            .name(faculty.getName())
                            .fanHours(hours[0])
                            .lectureHours(hours[1])
                            .seminarHours(hours[2])
                            .practicalHours(hours[3])
                            .labHours(hours[4])
                            .ratingHours(hours[5])
                            .auditoriyHours(hours[6])
                            .independentHours(hours[7])
                            .overallHours(hours[8])
                            .totalHours(hours[6])
                            .allocatedHours(hours[9])
                            .unallocatedHours(Math.max(0, hours[6] - hours[9]))
                            .build();
                })
                .toList();
    }

    private List<HoursByGroupResponse> buildDepartmentStats(AccessScope scope, Long facultyId, Long departmentId) {
        List<Department> departments;
        if (departmentId != null && departmentId > 0) {
            departments = departmentRepository.findById(departmentId).map(List::of).orElse(List.of());
        } else if (facultyId != null && facultyId > 0) {
            departments = departmentRepository.findByFaculty_Id(facultyId);
        } else if (!scope.isUnrestricted()
                && ((departmentId != null && departmentId < 0)
                || (facultyId != null && facultyId < 0))) {
            departments = List.of();
        } else {
            departments = departmentRepository.findAll();
        }

        return departments.stream()
                .map(department -> {
                    List<Subject> subjects = subjectRepository.findByDepartment_Id(department.getId());
                    int[] hours = sumHours(subjects);
                    return HoursByGroupResponse.builder()
                            .id(department.getId())
                            .name(department.getName())
                            .facultyId(department.getFaculty() != null ? department.getFaculty().getId() : null)
                            .facultyName(department.getFaculty() != null ? department.getFaculty().getName() : null)
                            .fanHours(hours[0])
                            .lectureHours(hours[1])
                            .seminarHours(hours[2])
                            .practicalHours(hours[3])
                            .labHours(hours[4])
                            .ratingHours(hours[5])
                            .auditoriyHours(hours[6])
                            .independentHours(hours[7])
                            .overallHours(hours[8])
                            .totalHours(hours[6])
                            .allocatedHours(hours[9])
                            .unallocatedHours(Math.max(0, hours[6] - hours[9]))
                            .build();
                })
                .toList();
    }

    private int[] sumHours(List<Subject> subjects) {
        int fanHours = 0;
        int lecture = 0;
        int seminar = 0;
        int practical = 0;
        int lab = 0;
        int rating = 0;
        int auditoriy = 0;
        int independent = 0;
        int overall = 0;
        int allocated = 0;
        for (Subject subject : subjects) {
            int subjectLecture = orZero(subject.getLectureHours());
            int subjectSeminar = orZero(subject.getSeminarHours());
            int subjectPractical = orZero(subject.getPracticalHours());
            int subjectLab = orZero(subject.getLabHours());
            int subjectRating = orZero(subject.getRatingHours());
            int subjectIndependent = orZero(subject.getIndependentStudyHours());
            int subjectAuditorium = auditoriumTotal(subject);
            int subjectFanHours = subjectLecture + subjectSeminar + subjectPractical + subjectLab + subjectIndependent;
            int subjectOverall = subjectAuditorium + subjectIndependent;
            fanHours += subjectFanHours;
            lecture += subjectLecture;
            seminar += subjectSeminar;
            practical += subjectPractical;
            lab += subjectLab;
            rating += subjectRating;
            auditoriy += subjectAuditorium;
            independent += subjectIndependent;
            overall += subjectOverall;
            allocated += Math.min(subjectAuditorium, (int) allocationRepository.sumHoursBySubjectId(subject.getId()));
        }
        return new int[]{fanHours, lecture, seminar, practical, lab, rating, auditoriy, independent, overall, allocated};
    }

    private void validateBucket(String label, int requested, int subjectTotal, int alreadyAllocatedOthers) {
        int remaining = Math.max(0, subjectTotal - alreadyAllocatedOthers);
        if (requested > remaining) {
            throw new BadRequestException(
                    label + " uchun faqat " + remaining + " soat mavjud (so'ralgan: " + requested + ")");
        }
    }

    private int[] allocatedSums(Long subjectId) {
        int lecture = 0;
        int seminar = 0;
        int practical = 0;
        int lab = 0;
        int rating = 0;
        for (WorkloadAllocation a : allocationRepository.findBySubject_Id(subjectId)) {
            lecture += orZero(a.getLectureHours());
            seminar += orZero(a.getSeminarHours());
            practical += orZero(a.getPracticalHours());
            lab += orZero(a.getLabHours());
            rating += orZero(a.getRatingHours());
        }
        return new int[]{lecture, seminar, practical, lab, rating};
    }

    private WorkloadRowResponse toRow(Subject subject) {
        int lecture = orZero(subject.getLectureHours());
        int seminar = orZero(subject.getSeminarHours());
        int practical = orZero(subject.getPracticalHours());
        int lab = orZero(subject.getLabHours());
        int rating = orZero(subject.getRatingHours());
        int independent = orZero(subject.getIndependentStudyHours());
        int total = lecture + seminar + practical + lab + rating;
        int allocated = (int) allocationRepository.sumHoursBySubjectId(subject.getId());
        int remaining = Math.max(0, total - allocated);
        Talabnoma source = talabnomaRepository.findByLinkedSubject_Id(subject.getId()).orElse(null);

        return WorkloadRowResponse.builder()
                .subjectId(subject.getId())
                .subjectName(subject.getName())
                .subjectCode(subject.getCode())
                .departmentId(subject.getDepartment() != null ? subject.getDepartment().getId() : null)
                .departmentName(subject.getDepartment() != null ? subject.getDepartment().getName() : null)
                .facultyId(subject.getDepartment() != null && subject.getDepartment().getFaculty() != null
                        ? subject.getDepartment().getFaculty().getId() : null)
                .facultyName(subject.getDepartment() != null && subject.getDepartment().getFaculty() != null
                        ? subject.getDepartment().getFaculty().getName() : null)
                .sourceFacultyId(source != null && source.getFromFaculty() != null
                        ? source.getFromFaculty().getId() : null)
                .sourceFacultyName(source != null && source.getFromFaculty() != null
                        ? source.getFromFaculty().getName() : null)
                .talabnomaCode(source != null ? source.getCode() : null)
                .semester(subject.getSemester())
                .courseYear(subject.getCourseYear() != null ? subject.getCourseYear() : 1)
                .lectureHours(lecture)
                .seminarHours(seminar)
                .practicalHours(practical)
                .labHours(lab)
                .ratingHours(rating)
                .independentStudyHours(independent)
                .totalHours(total)
                .allocatedHours(allocated)
                .remainingHours(remaining)
                .groupCount(orZero(subject.getGroupCount()))
                .studentCount(orZero(subject.getStudentCount()))
                .allocationStatus(statusOf(total, allocated))
                .build();
    }

    private WorkloadDetailResponse toDetail(Subject subject) {
        int[] sums = allocatedSums(subject.getId());
        int lecture = orZero(subject.getLectureHours());
        int seminar = orZero(subject.getSeminarHours());
        int practical = orZero(subject.getPracticalHours());
        int lab = orZero(subject.getLabHours());
        int rating = orZero(subject.getRatingHours());
        int independent = orZero(subject.getIndependentStudyHours());
        int total = lecture + seminar + practical + lab + rating;
        int allocated = sums[0] + sums[1] + sums[2] + sums[3] + sums[4];
        int totalSubjectHours = orZero(subject.getTotalSubjectHours());
        double credit = totalSubjectHours > 0 ? totalSubjectHours / 30.0 : 0;

        List<WorkloadDetailResponse.AllocationItem> items = allocationRepository.findBySubject_Id(subject.getId())
                .stream()
                .map(a -> {
                    int itemTotal = orZero(a.getLectureHours()) + orZero(a.getSeminarHours())
                            + orZero(a.getPracticalHours()) + orZero(a.getLabHours())
                            + orZero(a.getRatingHours());
                    return WorkloadDetailResponse.AllocationItem.builder()
                            .id(a.getId())
                            .teacherId(a.getTeacher().getId())
                            .teacherName(teacherDisplayName(a.getTeacher()))
                            .employmentStaffName(StringUtils.hasText(a.getEmploymentStaffName())
                                    ? a.getEmploymentStaffName()
                                    : a.getTeacher().getEmploymentStaffName())
                            .workloadRate(a.getWorkloadRate())
                            .lectureHours(orZero(a.getLectureHours()))
                            .seminarHours(orZero(a.getSeminarHours()))
                            .practicalHours(orZero(a.getPracticalHours()))
                            .labHours(orZero(a.getLabHours()))
                            .ratingHours(orZero(a.getRatingHours()))
                            .totalHours(itemTotal)
                            .groups(toGroupResponses(a.getGroups()))
                            .build();
                })
                .toList();

        Talabnoma source = talabnomaRepository.findByLinkedSubject_Id(subject.getId()).orElse(null);

        return WorkloadDetailResponse.builder()
                .subjectId(subject.getId())
                .subjectName(subject.getName())
                .subjectCode(subject.getCode())
                .departmentId(subject.getDepartment() != null ? subject.getDepartment().getId() : null)
                .departmentName(subject.getDepartment() != null ? subject.getDepartment().getName() : null)
                .facultyId(subject.getDepartment() != null && subject.getDepartment().getFaculty() != null
                        ? subject.getDepartment().getFaculty().getId() : null)
                .facultyName(subject.getDepartment() != null && subject.getDepartment().getFaculty() != null
                        ? subject.getDepartment().getFaculty().getName() : null)
                .sourceFacultyId(source != null && source.getFromFaculty() != null
                        ? source.getFromFaculty().getId() : null)
                .sourceFacultyName(source != null && source.getFromFaculty() != null
                        ? source.getFromFaculty().getName() : null)
                .talabnomaCode(source != null ? source.getCode() : null)
                .semester(subject.getSemester())
                .credit(Math.round(credit * 100.0) / 100.0)
                .totalSubjectHours(totalSubjectHours)
                .independentStudyHours(independent)
                .totalHours(total)
                .allocatedHours(allocated)
                .remainingHours(Math.max(0, total - allocated))
                .allocationStatus(statusOf(total, allocated))
                .lecture(bucket(lecture, sums[0]))
                .seminar(bucket(seminar, sums[1]))
                .practical(bucket(practical, sums[2]))
                .lab(bucket(lab, sums[3]))
                .rating(bucket(rating, sums[4]))
                .allocations(items)
                .build();
    }

    private WorkloadDetailResponse.HourBucket bucket(int total, int allocated) {
        return WorkloadDetailResponse.HourBucket.builder()
                .total(total)
                .allocated(allocated)
                .remaining(Math.max(0, total - allocated))
                .build();
    }

    private int distributableTotal(Subject subject) {
        return auditoriumTotal(subject);
    }

    private int auditoriumTotal(Subject subject) {
        int stored = orZero(subject.getAuditoriumHours());
        if (stored > 0) {
            return stored;
        }
        return orZero(subject.getLectureHours())
                + orZero(subject.getSeminarHours())
                + orZero(subject.getPracticalHours())
                + orZero(subject.getLabHours())
                + orZero(subject.getRatingHours());
    }

    private AllocationStatus statusOf(int total, int allocated) {
        if (total <= 0) {
            return AllocationStatus.UNALLOCATED;
        }
        if (allocated <= 0) {
            return AllocationStatus.UNALLOCATED;
        }
        if (allocated >= total) {
            return AllocationStatus.ALLOCATED;
        }
        return AllocationStatus.PARTIAL;
    }

    private AllocationStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return AllocationStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String loadLabel(int hours) {
        if (hours <= LIGHT_LOAD_MAX) {
            return "Kam yuklangan";
        }
        if (hours <= NORMAL_LOAD_MAX) {
            return "O'rtacha yuklangan";
        }
        return "Ko'p yuklangan";
    }

    private String teacherDisplayName(Teacher teacher) {
        if (StringUtils.hasText(teacher.getFullName())) {
            return teacher.getFullName();
        }
        return Stream.of(teacher.getSecondName(), teacher.getFirstName(), teacher.getThirdName())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
    }

    private Subject getSubjectOrThrow(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", id));
    }

    private Set<Group> resolveGroups(List<Long> groupIds, Department department) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Long> ids = groupIds.stream().filter(id -> id != null && id > 0).distinct().toList();
        if (ids.isEmpty()) {
            return new HashSet<>();
        }
        List<Group> found = groupRepository.findByIdIn(ids);
        if (found.size() != ids.size()) {
            throw new BadRequestException("Ba'zi guruhlar topilmadi");
        }
        if (department != null) {
            Long facultyId = department.getFaculty() != null ? department.getFaculty().getId() : null;
            for (Group group : found) {
                boolean sameDepartment = group.getDepartment() != null
                        && department.getId().equals(group.getDepartment().getId());
                boolean sameFaculty = facultyId != null
                        && group.getFaculty() != null
                        && facultyId.equals(group.getFaculty().getId());
                if (!sameDepartment && !sameFaculty) {
                    throw new BadRequestException(
                            "Guruh '" + group.getName() + "' ushbu fanning fakulteti/kafedrasiga tegishli emas");
                }
            }
        }
        return new HashSet<>(found);
    }

    private List<AllocatedGroupResponse> toGroupResponses(Set<Group> groups) {
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }
        return groups.stream()
                .sorted((a, b) -> String.valueOf(a.getName()).compareToIgnoreCase(String.valueOf(b.getName())))
                .map(g -> AllocatedGroupResponse.builder().id(g.getId()).name(g.getName()).build())
                .toList();
    }

    private int orZero(Integer value) {
        return value != null ? value : 0;
    }
}
