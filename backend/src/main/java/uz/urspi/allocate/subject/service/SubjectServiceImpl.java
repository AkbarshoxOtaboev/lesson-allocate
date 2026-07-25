package uz.urspi.allocate.subject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.subject.dto.SubjectRequest;
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.subject.repository.SubjectRepository;
import uz.urspi.allocate.subject.response.SubjectResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public SubjectResponse create(SubjectRequest request) {
        Subject subject = Subject.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .department(resolveDepartment(request.getDepartmentId()))
                .lectureHours(orZero(request.getLectureHours()))
                .practicalHours(orZero(request.getPracticalHours()))
                .labHours(orZero(request.getLabHours()))
                .seminarHours(orZero(request.getSeminarHours()))
                .independentStudyHours(orZero(request.getIndependentStudyHours()))
                .groupCount(orZero(request.getGroupCount()))
                .studentCount(orZero(request.getStudentCount()))
                .build();
        subject.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(subjectRepository.save(subject));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> findAll(Long facultyId, Long departmentId) {
        List<Subject> subjects;
        if (departmentId != null) {
            subjects = subjectRepository.findByDepartment_Id(departmentId);
        } else if (facultyId != null) {
            subjects = subjectRepository.findByDepartment_Faculty_Id(facultyId);
        } else {
            subjects = subjectRepository.findAll();
        }
        return subjects.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = getOrThrow(id);
        subject.setCode(request.getCode().trim());
        subject.setName(request.getName().trim());
        subject.setDepartment(resolveDepartment(request.getDepartmentId()));
        subject.setLectureHours(orZero(request.getLectureHours()));
        subject.setPracticalHours(orZero(request.getPracticalHours()));
        subject.setLabHours(orZero(request.getLabHours()));
        subject.setSeminarHours(orZero(request.getSeminarHours()));
        subject.setIndependentStudyHours(orZero(request.getIndependentStudyHours()));
        subject.setGroupCount(orZero(request.getGroupCount()));
        subject.setStudentCount(orZero(request.getStudentCount()));
        return toResponse(subjectRepository.save(subject));
    }

    @Override
    public void delete(Long id) {
        Subject subject = getOrThrow(id);
        subject.softDelete();
        subjectRepository.save(subject);
    }

    private Department resolveDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", departmentId));
    }

    private Subject getOrThrow(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", id));
    }

    private int orZero(Integer value) {
        return value != null ? value : 0;
    }

    private SubjectResponse toResponse(Subject subject) {
        int lecture = orZero(subject.getLectureHours());
        int practical = orZero(subject.getPracticalHours());
        int lab = orZero(subject.getLabHours());
        int seminar = orZero(subject.getSeminarHours());
        int independent = orZero(subject.getIndependentStudyHours());
        int totalHours = lecture + practical + lab + seminar;
        int overallHours = totalHours + independent;
        double credit = overallHours / 3.0;

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
                .lectureHours(lecture)
                .practicalHours(practical)
                .labHours(lab)
                .seminarHours(seminar)
                .independentStudyHours(independent)
                .totalHours(totalHours)
                .overallHours(overallHours)
                .credit(Math.round(credit * 100.0) / 100.0)
                .groupCount(orZero(subject.getGroupCount()))
                .studentCount(orZero(subject.getStudentCount()))
                .build();
    }
}
