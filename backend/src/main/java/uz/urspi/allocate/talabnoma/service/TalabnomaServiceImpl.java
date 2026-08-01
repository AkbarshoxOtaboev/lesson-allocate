package uz.urspi.allocate.talabnoma.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.academicyear.entity.AcademicYear;
import uz.urspi.allocate.academicyear.repository.AcademicYearRepository;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.direction.entity.Direction;
import uz.urspi.allocate.direction.repository.DirectionRepository;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.security.AccessScope;
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.subject.enums.EducationLanguage;
import uz.urspi.allocate.subject.enums.EducationType;
import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.subject.repository.SubjectRepository;
import uz.urspi.allocate.talabnoma.dto.TalabnomaRejectRequest;
import uz.urspi.allocate.talabnoma.dto.TalabnomaRequest;
import uz.urspi.allocate.talabnoma.entity.Talabnoma;
import uz.urspi.allocate.talabnoma.enums.TalabnomaStatus;
import uz.urspi.allocate.talabnoma.repository.TalabnomaRepository;
import uz.urspi.allocate.talabnoma.response.TalabnomaResponse;
import uz.urspi.allocate.talabnoma.response.TalabnomaStatsResponse;
import uz.urspi.allocate.user.entity.User;
import uz.urspi.allocate.workload.entity.WorkloadAllocation;
import uz.urspi.allocate.workload.repository.WorkloadAllocationRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TalabnomaServiceImpl implements TalabnomaService {

    private final TalabnomaRepository talabnomaRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final DirectionRepository directionRepository;
    private final SubjectRepository subjectRepository;
    private final WorkloadAllocationRepository allocationRepository;

    @Override
    @Auditable(entity = "Talabnoma", action = AuditAction.CREATE)
    public TalabnomaResponse create(TalabnomaRequest request) {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new BadRequestException("Foydalanuvchi topilmadi");
        }
        if (!user.hasRole("DEKAN") && !user.hasRole("SUPER_ADMIN") && !user.hasRole("ADMIN")) {
            throw new BadRequestException("Faqat dekan yoki admin talabnoma yubora oladi");
        }

        Faculty fromFaculty = user.getFaculty();
        if (fromFaculty == null && (user.hasRole("SUPER_ADMIN") || user.hasRole("ADMIN"))) {
            Department target = getDepartment(request.getToDepartmentId());
            fromFaculty = target.getFaculty();
        }
        if (fromFaculty == null) {
            throw new BadRequestException("Dekan uchun fakultet biriktirilmagan");
        }

        Department toDepartment = getDepartment(request.getToDepartmentId());
        int lecture = nz(request.getLectureHours());
        int practical = nz(request.getPracticalHours());
        int lab = nz(request.getLabHours());
        int seminar = nz(request.getSeminarHours());
        int independent = nz(request.getIndependentStudyHours());
        int rating = nz(request.getRatingHours());
        int totalSubjectHours = nz(request.getTotalSubjectHours());
        int allocatedNonRating = lecture + practical + lab + seminar + independent;
        int auditoriy = lecture + practical + lab + seminar + rating;

        if (totalSubjectHours <= 0) {
            totalSubjectHours = allocatedNonRating;
        }
        if (totalSubjectHours > 0 && allocatedNonRating != totalSubjectHours) {
            throw new BadRequestException(
                    "Umumiy fan soati to'liq taqsimlanmagan (qolgan: "
                            + Math.max(0, totalSubjectHours - allocatedNonRating) + ")");
        }
        if (auditoriy + independent <= 0 && totalSubjectHours <= 0) {
            throw new BadRequestException("Kamida bitta soat turi 0 dan katta bo'lishi kerak");
        }

        AcademicYear year = null;
        if (request.getAcademicYearId() != null) {
            year = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> ResourceNotFoundException.of("AcademicYear", request.getAcademicYearId()));
        } else {
            year = academicYearRepository.findAll().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCurrentYear()))
                    .findFirst()
                    .orElse(null);
        }

        Direction direction = null;
        if (request.getDirectionId() != null) {
            direction = directionRepository.findById(request.getDirectionId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Direction", request.getDirectionId()));
        }

        Talabnoma entity = Talabnoma.builder()
                .code(nextCode())
                .fromFaculty(fromFaculty)
                .toDepartment(toDepartment)
                .createdBy(user)
                .subjectName(request.getSubjectName().trim())
                .subjectCode(StringUtils.hasText(request.getSubjectCode())
                        ? request.getSubjectCode().trim()
                        : null)
                .academicYear(year)
                .direction(direction)
                .semester(request.getSemester() != null ? request.getSemester() : Semester.AUTUMN)
                .educationType(request.getEducationType() != null
                        ? request.getEducationType()
                        : EducationType.KUNDUZGI)
                .educationLanguage(request.getEducationLanguage() != null
                        ? request.getEducationLanguage()
                        : EducationLanguage.UZB)
                .totalSubjectHours(totalSubjectHours)
                .lectureHours(lecture)
                .practicalHours(practical)
                .labHours(lab)
                .seminarHours(seminar)
                .independentStudyHours(independent)
                .ratingHours(rating)
                .totalHours(auditoriy)
                .groupCount(nz(request.getGroupCount()))
                .studentCount(nz(request.getStudentCount()))
                .courseYear(normalizeCourseYear(request.getCourseYear()))
                .requestStatus(TalabnomaStatus.NEW)
                .note(request.getNote())
                .build();
        entity.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(talabnomaRepository.save(entity));
    }

    @Override
    @Auditable(entity = "Talabnoma", action = AuditAction.UPDATE)
    public TalabnomaResponse update(Long id, TalabnomaRequest request) {
        User user = SecurityUtils.getCurrentUser();
        if (user == null || !user.hasRole("DEKAN")) {
            throw new BadRequestException("Faqat dekan talabnomani tahrirlashi mumkin");
        }

        Talabnoma entity = getOrThrow(id);
        if (entity.getRequestStatus() != TalabnomaStatus.NEW) {
            throw new BadRequestException("Faqat yangi (kutilmoqda) holatdagi talabnomani tahrirlash mumkin");
        }

        Faculty userFaculty = user.getFaculty();
        if (userFaculty == null
                || entity.getFromFaculty() == null
                || !userFaculty.getId().equals(entity.getFromFaculty().getId())) {
            throw new BadRequestException("Faqat o'z fakultetingizdan yuborilgan talabnomani tahrirlashingiz mumkin");
        }

        Department toDepartment = getDepartment(request.getToDepartmentId());
        int lecture = nz(request.getLectureHours());
        int practical = nz(request.getPracticalHours());
        int lab = nz(request.getLabHours());
        int seminar = nz(request.getSeminarHours());
        int independent = nz(request.getIndependentStudyHours());
        int rating = nz(request.getRatingHours());
        int totalSubjectHours = nz(request.getTotalSubjectHours());
        int allocatedNonRating = lecture + practical + lab + seminar + independent;
        int auditoriy = lecture + practical + lab + seminar + rating;

        if (totalSubjectHours <= 0) {
            totalSubjectHours = allocatedNonRating;
        }
        if (totalSubjectHours > 0 && allocatedNonRating != totalSubjectHours) {
            throw new BadRequestException(
                    "Umumiy fan soati to'liq taqsimlanmagan (qolgan: "
                            + Math.max(0, totalSubjectHours - allocatedNonRating) + ")");
        }
        if (auditoriy + independent <= 0 && totalSubjectHours <= 0) {
            throw new BadRequestException("Kamida bitta soat turi 0 dan katta bo'lishi kerak");
        }

        AcademicYear year = entity.getAcademicYear();
        if (request.getAcademicYearId() != null) {
            year = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> ResourceNotFoundException.of("AcademicYear", request.getAcademicYearId()));
        }

        Direction direction = null;
        if (request.getDirectionId() != null) {
            direction = directionRepository.findById(request.getDirectionId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Direction", request.getDirectionId()));
        }

        entity.setToDepartment(toDepartment);
        entity.setSubjectName(request.getSubjectName().trim());
        entity.setSubjectCode(StringUtils.hasText(request.getSubjectCode())
                ? request.getSubjectCode().trim()
                : null);
        entity.setAcademicYear(year);
        entity.setDirection(direction);
        entity.setSemester(request.getSemester() != null ? request.getSemester() : Semester.AUTUMN);
        entity.setEducationType(request.getEducationType() != null
                ? request.getEducationType()
                : EducationType.KUNDUZGI);
        entity.setEducationLanguage(request.getEducationLanguage() != null
                ? request.getEducationLanguage()
                : EducationLanguage.UZB);
        entity.setTotalSubjectHours(totalSubjectHours);
        entity.setLectureHours(lecture);
        entity.setPracticalHours(practical);
        entity.setLabHours(lab);
        entity.setSeminarHours(seminar);
        entity.setIndependentStudyHours(independent);
        entity.setRatingHours(rating);
        entity.setTotalHours(auditoriy);
        entity.setGroupCount(nz(request.getGroupCount()));
        entity.setStudentCount(nz(request.getStudentCount()));
        entity.setCourseYear(normalizeCourseYear(request.getCourseYear()));
        entity.setNote(request.getNote());

        return toResponse(talabnomaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TalabnomaResponse> findAll(Long facultyId, Long departmentId, TalabnomaStatus status) {
        AccessScope scope = AccessScope.ofCurrentUser();
        Long effectiveFacultyId = scope.resolveFacultyId(facultyId);
        Long effectiveDepartmentId = scope.resolveDepartmentId(departmentId);

        List<Talabnoma> list;
        if (!scope.isUnrestricted()
                && ((effectiveDepartmentId != null && effectiveDepartmentId < 0)
                || (effectiveFacultyId != null && effectiveFacultyId < 0 && effectiveDepartmentId == null))) {
            list = List.of();
        } else if (effectiveDepartmentId != null) {
            list = talabnomaRepository.findByToDepartment_IdOrderByCreatedAtDesc(effectiveDepartmentId);
        } else if (effectiveFacultyId != null) {
            // Dekan: o'zi yuborganlari + fakultetdagi kafedralarga kelganlari
            User user = SecurityUtils.getCurrentUser();
            if (user != null && user.hasRole("DEKAN") && !user.hasRole("ADMIN") && !user.hasRole("SUPER_ADMIN")) {
                list = new ArrayList<>(talabnomaRepository.findByFromFaculty_IdOrderByCreatedAtDesc(effectiveFacultyId));
            } else {
                list = talabnomaRepository.findByToDepartment_Faculty_IdOrderByCreatedAtDesc(effectiveFacultyId);
            }
        } else {
            list = talabnomaRepository.findAll().stream()
                    .sorted(Comparator.comparing(Talabnoma::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        }

        return list.stream()
                .filter(t -> status == null || t.getRequestStatus() == status)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TalabnomaResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Auditable(entity = "Talabnoma", action = AuditAction.UPDATE)
    public TalabnomaResponse accept(Long id) {
        Talabnoma entity = getOrThrow(id);
        ensureCanDecide(entity);
        if (entity.getRequestStatus() != TalabnomaStatus.NEW) {
            throw new BadRequestException("Faqat yangi talabnomani qabul qilish mumkin");
        }

        Subject subject = Subject.builder()
                .code(StringUtils.hasText(entity.getSubjectCode())
                        ? entity.getSubjectCode()
                        : entity.getCode())
                .name(entity.getSubjectName())
                .department(entity.getToDepartment())
                .academicYear(entity.getAcademicYear())
                .direction(entity.getDirection())
                .semester(entity.getSemester())
                .educationType(entity.getEducationType() != null
                        ? entity.getEducationType()
                        : EducationType.KUNDUZGI)
                .educationLanguage(entity.getEducationLanguage() != null
                        ? entity.getEducationLanguage()
                        : EducationLanguage.UZB)
                .totalSubjectHours(entity.getTotalSubjectHours() != null && entity.getTotalSubjectHours() > 0
                        ? entity.getTotalSubjectHours()
                        : entity.getTotalHours())
                .lectureHours(entity.getLectureHours())
                .practicalHours(entity.getPracticalHours())
                .labHours(entity.getLabHours())
                .seminarHours(entity.getSeminarHours())
                .independentStudyHours(entity.getIndependentStudyHours())
                .ratingHours(entity.getRatingHours())
                .groupCount(entity.getGroupCount())
                .studentCount(entity.getStudentCount())
                .courseYear(normalizeCourseYear(entity.getCourseYear()))
                .build();
        subject.setCreatedUsername(SecurityUtils.getCurrentUsername());
        subject = subjectRepository.save(subject);

        entity.setLinkedSubject(subject);
        entity.setRequestStatus(TalabnomaStatus.ACCEPTED);
        return toResponse(talabnomaRepository.save(entity));
    }

    @Override
    @Auditable(entity = "Talabnoma", action = AuditAction.UPDATE)
    public TalabnomaResponse reject(Long id, TalabnomaRejectRequest request) {
        Talabnoma entity = getOrThrow(id);
        ensureCanDecide(entity);
        if (entity.getRequestStatus() != TalabnomaStatus.NEW) {
            throw new BadRequestException("Faqat yangi talabnomani rad etish mumkin");
        }
        entity.setRequestStatus(TalabnomaStatus.REJECTED);
        entity.setRejectReason(request != null ? request.getReason() : null);
        return toResponse(talabnomaRepository.save(entity));
    }

    @Override
    @Auditable(entity = "Talabnoma", action = AuditAction.DELETE)
    public void delete(Long id) {
        Talabnoma entity = getOrThrow(id);
        User user = SecurityUtils.getCurrentUser();
        boolean admin = user != null && (user.hasRole("SUPER_ADMIN") || user.hasRole("ADMIN"));
        boolean owner = user != null && entity.getCreatedBy() != null
                && entity.getCreatedBy().getId().equals(user.getId());
        if (!admin && !owner) {
            throw new BadRequestException("O'chirishga ruxsat yo'q");
        }
        if (entity.getRequestStatus() == TalabnomaStatus.ACCEPTED
                || entity.getRequestStatus() == TalabnomaStatus.PARTIAL
                || entity.getRequestStatus() == TalabnomaStatus.ALLOCATED) {
            throw new BadRequestException("Qabul qilingan / taqsimlangan talabnomani o'chirib bo'lmaydi");
        }
        entity.softDelete();
        talabnomaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public TalabnomaStatsResponse stats(Long facultyId, Long departmentId) {
        List<TalabnomaResponse> all = findAll(facultyId, departmentId, null);
        return TalabnomaStatsResponse.builder()
                .total(all.size())
                .pending(all.stream().filter(t -> t.getRequestStatus() == TalabnomaStatus.NEW).count())
                .accepted(all.stream().filter(t -> t.getRequestStatus() == TalabnomaStatus.ACCEPTED).count())
                .rejected(all.stream().filter(t -> t.getRequestStatus() == TalabnomaStatus.REJECTED).count())
                .allocated(all.stream().filter(t -> t.getRequestStatus() == TalabnomaStatus.ALLOCATED).count())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long countNewForCurrentUser() {
        AccessScope scope = AccessScope.ofCurrentUser();
        if (scope.getDepartmentId() != null && scope.getDepartmentId() > 0) {
            return talabnomaRepository.countByToDepartment_IdAndRequestStatus(
                    scope.getDepartmentId(), TalabnomaStatus.NEW);
        }
        return 0;
    }

    @Override
    public void refreshStatusForSubject(Long subjectId) {
        if (subjectId == null) return;
        talabnomaRepository.findByLinkedSubject_Id(subjectId).ifPresent(entity -> {
            if (entity.getRequestStatus() == TalabnomaStatus.REJECTED
                    || entity.getRequestStatus() == TalabnomaStatus.NEW) {
                return;
            }
            int allocated = (int) allocationRepository.sumHoursBySubjectId(subjectId);
            int total = nz(entity.getTotalHours());
            if (allocated <= 0) {
                entity.setRequestStatus(TalabnomaStatus.ACCEPTED);
            } else if (allocated >= total) {
                entity.setRequestStatus(TalabnomaStatus.ALLOCATED);
            } else {
                entity.setRequestStatus(TalabnomaStatus.PARTIAL);
            }
            talabnomaRepository.save(entity);
        });
    }

    private void ensureCanDecide(Talabnoma entity) {
        AccessScope scope = AccessScope.ofCurrentUser();
        if (scope.isUnrestricted()) return;
        if (scope.getDepartmentId() != null
                && scope.getDepartmentId().equals(entity.getToDepartment().getId())) {
            return;
        }
        throw new BadRequestException("Bu talabnomani faqat qabul qiluvchi kafedra boshqara oladi");
    }

    private String nextCode() {
        String prefix = "TL-" + LocalDate.now().getYear() + "-";
        long seq = talabnomaRepository.count() + 1;
        String code;
        do {
            code = prefix + String.format("%03d", seq++);
        } while (talabnomaRepository.findByCode(code).isPresent());
        return code;
    }

    private Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", id));
    }

    private Talabnoma getOrThrow(Long id) {
        return talabnomaRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Talabnoma", id));
    }

    private TalabnomaResponse toResponse(Talabnoma t) {
        int allocatedHours = 0;
        List<TalabnomaResponse.AllocatedTeacherInfo> teachers = List.of();
        if (t.getLinkedSubject() != null) {
            Long subjectId = t.getLinkedSubject().getId();
            allocatedHours = (int) allocationRepository.sumHoursBySubjectId(subjectId);
            List<WorkloadAllocation> allocations = allocationRepository.findBySubject_Id(subjectId);
            teachers = allocations.stream()
                    .map(a -> {
                        int hours = nz(a.getLectureHours()) + nz(a.getSeminarHours())
                                + nz(a.getPracticalHours()) + nz(a.getLabHours())
                                + nz(a.getRatingHours());
                        return TalabnomaResponse.AllocatedTeacherInfo.builder()
                                .teacherId(a.getTeacher().getId())
                                .teacherName(a.getTeacher().getFullName() != null
                                        ? a.getTeacher().getFullName()
                                        : a.getTeacher().getShortName())
                                .hours(hours)
                                .build();
                    })
                    .filter(info -> info.getHours() > 0)
                    .toList();
        }

        Department dept = t.getToDepartment();
        Faculty toFaculty = dept != null ? dept.getFaculty() : null;
        User creator = t.getCreatedBy();
        Direction direction = t.getDirection();
        TalabnomaStatus liveStatus = resolveLiveStatus(t, allocatedHours);

        return TalabnomaResponse.builder()
                .id(t.getId())
                .code(t.getCode())
                .fromFacultyId(t.getFromFaculty() != null ? t.getFromFaculty().getId() : null)
                .fromFacultyName(t.getFromFaculty() != null ? t.getFromFaculty().getName() : null)
                .toDepartmentId(dept != null ? dept.getId() : null)
                .toDepartmentName(dept != null ? dept.getName() : null)
                .toFacultyId(toFaculty != null ? toFaculty.getId() : null)
                .toFacultyName(toFaculty != null ? toFaculty.getName() : null)
                .subjectName(t.getSubjectName())
                .subjectCode(t.getSubjectCode())
                .academicYearId(t.getAcademicYear() != null ? t.getAcademicYear().getId() : null)
                .academicYearName(t.getAcademicYear() != null ? t.getAcademicYear().getName() : null)
                .directionId(direction != null ? direction.getId() : null)
                .directionCode(direction != null ? direction.getDirectionCode() : null)
                .directionName(direction != null ? direction.getDirectionName() : null)
                .courseYear(normalizeCourseYear(t.getCourseYear()))
                .semester(t.getSemester())
                .educationType(t.getEducationType())
                .educationLanguage(t.getEducationLanguage())
                .totalSubjectHours(t.getTotalSubjectHours())
                .lectureHours(t.getLectureHours())
                .practicalHours(t.getPracticalHours())
                .labHours(t.getLabHours())
                .seminarHours(t.getSeminarHours())
                .independentStudyHours(t.getIndependentStudyHours())
                .ratingHours(t.getRatingHours())
                .totalHours(t.getTotalHours())
                .groupCount(t.getGroupCount())
                .studentCount(t.getStudentCount())
                .allocatedHours(allocatedHours)
                .requestStatus(liveStatus)
                .note(t.getNote())
                .rejectReason(t.getRejectReason())
                .linkedSubjectId(t.getLinkedSubject() != null ? t.getLinkedSubject().getId() : null)
                .createdByName(creator != null
                        ? (StringUtils.hasText(creator.getFullName()) ? creator.getFullName() : creator.getUsername())
                        : null)
                .createdAt(t.getCreatedAt())
                .allocatedTeachers(teachers)
                .build();
    }

    private TalabnomaStatus resolveLiveStatus(Talabnoma t, int allocatedHours) {
        TalabnomaStatus current = t.getRequestStatus();
        if (current == TalabnomaStatus.NEW || current == TalabnomaStatus.REJECTED) {
            return current;
        }
        if (t.getLinkedSubject() == null) {
            return current;
        }
        int total = nz(t.getTotalHours());
        if (allocatedHours <= 0) {
            return TalabnomaStatus.ACCEPTED;
        }
        if (total > 0 && allocatedHours >= total) {
            return TalabnomaStatus.ALLOCATED;
        }
        return TalabnomaStatus.PARTIAL;
    }

    private int normalizeCourseYear(Integer courseYear) {
        if (courseYear == null || courseYear < 1) {
            return 1;
        }
        return Math.min(courseYear, 5);
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
