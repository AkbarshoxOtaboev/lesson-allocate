package uz.urspi.allocate.teacher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.teacher.dto.TeacherRequest;
import uz.urspi.allocate.teacher.entity.Teacher;
import uz.urspi.allocate.teacher.repository.TeacherRepository;
import uz.urspi.allocate.teacher.response.TeacherResponse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;

    @Override
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
        List<Teacher> teachers;
        if (departmentId != null) {
            teachers = teacherRepository.findByDepartment_Id(departmentId);
        } else if (facultyId != null) {
            teachers = teacherRepository.findByDepartment_Faculty_Id(facultyId);
        } else {
            teachers = teacherRepository.findAll();
        }
        return teachers.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
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
