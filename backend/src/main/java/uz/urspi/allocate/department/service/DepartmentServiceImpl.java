package uz.urspi.allocate.department.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.dto.NameRequest;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.department.response.DepartmentResponse;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.faculty.repository.FacultyRepository;
import uz.urspi.allocate.teacher.repository.TeacherRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public DepartmentResponse create(NameRequest request) {
        Department department = Department.builder()
                .name(request.getName())
                .faculty(resolveFaculty(request.getFacultyId()))
                .build();
        department.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll(Long facultyId) {
        List<Department> departments = facultyId == null
                ? departmentRepository.findAll()
                : departmentRepository.findByFaculty_Id(facultyId);
        return departments.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public DepartmentResponse update(Long id, NameRequest request) {
        Department department = getOrThrow(id);
        department.setName(request.getName());
        if (request.getFacultyId() != null) {
            department.setFaculty(resolveFaculty(request.getFacultyId()));
        }
        return toResponse(departmentRepository.save(department));
    }

    @Override
    public void delete(Long id) {
        Department department = getOrThrow(id);
        department.softDelete();
        departmentRepository.save(department);
    }

    private Faculty resolveFaculty(Long facultyId) {
        if (facultyId == null) {
            return null;
        }
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Faculty", facultyId));
    }

    private Department getOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", id));
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .status(department.getStatus())
                .facultyId(department.getFaculty() != null ? department.getFaculty().getId() : null)
                .facultyName(department.getFaculty() != null ? department.getFaculty().getName() : null)
                .hemisId(department.getHemisId())
                .code(department.getCode())
                .teacherCount(teacherRepository.countByDepartment_Id(department.getId()))
                .build();
    }
}
