package uz.urspi.allocate.subject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.direction.entity.Direction;
import uz.urspi.allocate.direction.repository.DirectionRepository;
import uz.urspi.allocate.security.AccessScope;
import uz.urspi.allocate.subject.dto.SubjectRequest;
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.subject.repository.SubjectRepository;
import uz.urspi.allocate.subject.response.SubjectResponse;
import uz.urspi.allocate.talabnoma.entity.Talabnoma;
import uz.urspi.allocate.talabnoma.repository.TalabnomaRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;
    private final uz.urspi.allocate.academicyear.repository.AcademicYearRepository academicYearRepository;
    private final DirectionRepository directionRepository;
    private final TalabnomaRepository talabnomaRepository;

    @Override
    @Auditable(entity = "Subject", action = AuditAction.CREATE)
    public SubjectResponse create(SubjectRequest request) {
        validateHours(request);
        Subject subject = Subject.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .department(resolveDepartment(request.getDepartmentId()))
                .academicYear(resolveAcademicYear(request.getAcademicYearId()))
                .direction(resolveDirection(request.getDirectionId()))
                .semester(request.getSemester())
                .educationType(request.getEducationType())
                .educationLanguage(request.getEducationLanguage())
                .totalSubjectHours(orZero(request.getTotalSubjectHours()))
                .lectureHours(orZero(request.getLectureHours()))
                .practicalHours(orZero(request.getPracticalHours()))
                .labHours(orZero(request.getLabHours()))
                .seminarHours(orZero(request.getSeminarHours()))
                .independentStudyHours(orZero(request.getIndependentStudyHours()))
                .ratingHours(orZero(request.getRatingHours()))
                .groupCount(orZero(request.getGroupCount()))
                .studentCount(orZero(request.getStudentCount()))
                .build();
        subject.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(subjectRepository.save(subject));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> findAll(Long facultyId, Long departmentId, Semester semester) {
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
        if (subjects.isEmpty()) {
            return List.of();
        }
        Map<Long, Talabnoma> bySubjectId = talabnomaRepository
                .findByLinkedSubject_IdIn(subjects.stream().map(Subject::getId).toList())
                .stream()
                .filter(t -> t.getLinkedSubject() != null)
                .collect(Collectors.toMap(t -> t.getLinkedSubject().getId(), Function.identity(), (a, b) -> a));

        return subjects.stream().map(s -> toResponse(s, bySubjectId.get(s.getId()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        Subject subject = getOrThrow(id);
        Talabnoma talabnoma = talabnomaRepository.findByLinkedSubject_Id(id).orElse(null);
        return toResponse(subject, talabnoma);
    }

    @Override
    @Auditable(entity = "Subject", action = AuditAction.UPDATE)
    public SubjectResponse update(Long id, SubjectRequest request) {
        validateHours(request);
        Subject subject = getOrThrow(id);
        subject.setCode(request.getCode().trim());
        subject.setName(request.getName().trim());
        subject.setDepartment(resolveDepartment(request.getDepartmentId()));
        subject.setAcademicYear(resolveAcademicYear(request.getAcademicYearId()));
        subject.setDirection(resolveDirection(request.getDirectionId()));
        subject.setSemester(request.getSemester());
        subject.setEducationType(request.getEducationType());
        subject.setEducationLanguage(request.getEducationLanguage());
        subject.setTotalSubjectHours(orZero(request.getTotalSubjectHours()));
        subject.setLectureHours(orZero(request.getLectureHours()));
        subject.setPracticalHours(orZero(request.getPracticalHours()));
        subject.setLabHours(orZero(request.getLabHours()));
        subject.setSeminarHours(orZero(request.getSeminarHours()));
        subject.setIndependentStudyHours(orZero(request.getIndependentStudyHours()));
        subject.setRatingHours(orZero(request.getRatingHours()));
        subject.setGroupCount(orZero(request.getGroupCount()));
        subject.setStudentCount(orZero(request.getStudentCount()));
        return toResponse(subjectRepository.save(subject));
    }

    @Override
    @Auditable(entity = "Subject", action = AuditAction.DELETE)
    public void delete(Long id) {
        Subject subject = getOrThrow(id);
        subject.softDelete();
        subjectRepository.save(subject);
    }

    private void validateHours(SubjectRequest request) {
        int total = orZero(request.getTotalSubjectHours());
        int allocated = orZero(request.getLectureHours())
                + orZero(request.getPracticalHours())
                + orZero(request.getLabHours())
                + orZero(request.getSeminarHours())
                + orZero(request.getIndependentStudyHours());
        if (total <= 0) {
            throw new BadRequestException("Umumiy fan soati 0 dan katta bo'lishi kerak");
        }
        if (allocated != total) {
            throw new BadRequestException(
                    "Umumiy fan soati to'liq taqsimlanishi kerak. Umumiy: "
                            + total + ", taqsimlangan: " + allocated
                            + ", qolgan: " + (total - allocated));
        }
    }

    private Department resolveDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", departmentId));
    }

    private uz.urspi.allocate.academicyear.entity.AcademicYear resolveAcademicYear(Long id) {
        if (id == null) return null;
        return academicYearRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("AcademicYear", id));
    }

    private Direction resolveDirection(Long id) {
        if (id == null) return null;
        return directionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Direction", id));
    }

    private Subject getOrThrow(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", id));
    }

    private int orZero(Integer value) {
        return value != null ? value : 0;
    }

    private SubjectResponse toResponse(Subject subject) {
        Talabnoma talabnoma = talabnomaRepository.findByLinkedSubject_Id(subject.getId()).orElse(null);
        return toResponse(subject, talabnoma);
    }

    private SubjectResponse toResponse(Subject subject, Talabnoma talabnoma) {
        int lecture = orZero(subject.getLectureHours());
        int practical = orZero(subject.getPracticalHours());
        int lab = orZero(subject.getLabHours());
        int seminar = orZero(subject.getSeminarHours());
        int independent = orZero(subject.getIndependentStudyHours());
        int rating = orZero(subject.getRatingHours());
        int totalSubjectHours = orZero(subject.getTotalSubjectHours());
        int totalHours = lecture + practical + lab + seminar + rating;
        int overallHours = totalHours + independent;
        double credit = totalSubjectHours > 0 ? totalSubjectHours / 30.0 : 0;

        return SubjectResponse.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .name(subject.getName())
                .status(subject.getStatus())
                .departmentId(subject.getDepartment() != null ? subject.getDepartment().getId() : null)
                .departmentName(subject.getDepartment() != null ? subject.getDepartment().getName() : null)
                .facultyId(subject.getDepartment() != null && subject.getDepartment().getFaculty() != null
                        ? subject.getDepartment().getFaculty().getId() : null)
                .facultyName(subject.getDepartment() != null && subject.getDepartment().getFaculty() != null
                        ? subject.getDepartment().getFaculty().getName() : null)
                .sourceFacultyId(talabnoma != null && talabnoma.getFromFaculty() != null
                        ? talabnoma.getFromFaculty().getId() : null)
                .sourceFacultyName(talabnoma != null && talabnoma.getFromFaculty() != null
                        ? talabnoma.getFromFaculty().getName() : null)
                .talabnomaCode(talabnoma != null ? talabnoma.getCode() : null)
                .academicYearId(subject.getAcademicYear() != null ? subject.getAcademicYear().getId() : null)
                .academicYearName(subject.getAcademicYear() != null ? subject.getAcademicYear().getName() : null)
                .directionId(subject.getDirection() != null ? subject.getDirection().getId() : null)
                .directionCode(subject.getDirection() != null ? subject.getDirection().getDirectionCode() : null)
                .directionName(subject.getDirection() != null ? subject.getDirection().getDirectionName() : null)
                .semester(subject.getSemester())
                .educationType(subject.getEducationType())
                .educationLanguage(subject.getEducationLanguage())
                .totalSubjectHours(totalSubjectHours)
                .lectureHours(lecture)
                .practicalHours(practical)
                .labHours(lab)
                .seminarHours(seminar)
                .independentStudyHours(independent)
                .ratingHours(rating)
                .totalHours(totalHours)
                .overallHours(overallHours)
                .credit(Math.round(credit * 100.0) / 100.0)
                .groupCount(orZero(subject.getGroupCount()))
                .studentCount(orZero(subject.getStudentCount()))
                .build();
    }
}
