package uz.urspi.allocate.faculty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.faculty.dto.NameRequest;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.faculty.repository.FacultyRepository;
import uz.urspi.allocate.faculty.response.FacultyResponse;
import uz.urspi.allocate.security.AccessScope;
import uz.urspi.allocate.teacher.repository.TeacherRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public FacultyResponse create(NameRequest request) {
        Faculty faculty = Faculty.builder().name(request.getName()).build();
        faculty.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(facultyRepository.save(faculty));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyResponse> findAll() {
        AccessScope scope = AccessScope.ofCurrentUser();
        List<Faculty> faculties;
        if (scope.isUnrestricted()) {
            faculties = facultyRepository.findAll();
        } else if (scope.getFacultyId() != null && scope.getFacultyId() > 0) {
            faculties = facultyRepository.findById(scope.getFacultyId()).stream().toList();
        } else {
            faculties = List.of();
        }
        return faculties.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public FacultyResponse update(Long id, NameRequest request) {
        Faculty faculty = getOrThrow(id);
        faculty.setName(request.getName());
        return toResponse(facultyRepository.save(faculty));
    }

    @Override
    public void delete(Long id) {
        Faculty faculty = getOrThrow(id);
        faculty.softDelete();
        facultyRepository.save(faculty);
    }

    private Faculty getOrThrow(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Faculty", id));
    }

    private FacultyResponse toResponse(Faculty faculty) {
        return FacultyResponse.builder()
                .id(faculty.getId())
                .name(faculty.getName())
                .status(faculty.getStatus())
                .hemisId(faculty.getHemisId())
                .code(faculty.getCode())
                .hemisActive(faculty.getHemisActive())
                .structureTypeCode(faculty.getStructureTypeCode())
                .departmentCount(departmentRepository.countByFaculty_Id(faculty.getId()))
                .teacherCount(teacherRepository.countByDepartment_Faculty_Id(faculty.getId()))
                .build();
    }
}
