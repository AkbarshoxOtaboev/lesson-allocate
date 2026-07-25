package uz.urspi.allocate.hemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.faculty.repository.FacultyRepository;
import uz.urspi.allocate.hemis.client.HemisClient;
import uz.urspi.allocate.hemis.dto.HemisDepartmentDto;
import uz.urspi.allocate.hemis.dto.HemisDepartmentQuery;
import uz.urspi.allocate.hemis.dto.HemisEmployeeDto;
import uz.urspi.allocate.hemis.dto.HemisEmployeeQuery;
import uz.urspi.allocate.hemis.response.HemisDepartmentListResponse;
import uz.urspi.allocate.hemis.response.HemisEmployeeListResponse;
import uz.urspi.allocate.hemis.response.HemisSyncResult;
import uz.urspi.allocate.teacher.entity.Teacher;
import uz.urspi.allocate.teacher.repository.TeacherRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class HemisSyncServiceImpl implements HemisSyncService {

    private final HemisClient hemisClient;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    @Transactional(readOnly = true)
    public HemisDepartmentListResponse fetchDepartments(HemisDepartmentQuery query) {
        return hemisClient.fetchDepartments(normalize(query, false));
    }

    @Override
    public HemisSyncResult syncFaculties(HemisDepartmentQuery query) {
        List<HemisDepartmentDto> items = hemisClient.fetchDepartments(normalize(query, true)).getItems();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (HemisDepartmentDto dto : items) {
            if (dto.getId() == null || !StringUtils.hasText(dto.getName())) {
                skipped++;
                continue;
            }
            Faculty faculty = facultyRepository.findByHemisId(dto.getId()).orElse(null);
            if (faculty == null) {
                faculty = Faculty.builder()
                        .name(dto.getName())
                        .hemisId(dto.getId())
                        .code(dto.getCode())
                        .hemisActive(Boolean.TRUE.equals(dto.getActive()))
                        .structureTypeCode(codeOf(dto))
                        .parentHemisId(dto.getParent())
                        .build();
                faculty.setCreatedUsername(SecurityUtils.getCurrentUsername());
                facultyRepository.save(faculty);
                created++;
            } else {
                faculty.setName(dto.getName());
                faculty.setCode(dto.getCode());
                faculty.setHemisActive(Boolean.TRUE.equals(dto.getActive()));
                faculty.setStructureTypeCode(codeOf(dto));
                faculty.setParentHemisId(dto.getParent());
                facultyRepository.save(faculty);
                updated++;
            }
        }

        return result(items.size(), created, updated, skipped);
    }

    @Override
    public HemisSyncResult syncDepartments(HemisDepartmentQuery query) {
        List<HemisDepartmentDto> items = hemisClient.fetchDepartments(normalize(query, true)).getItems();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (HemisDepartmentDto dto : items) {
            if (dto.getId() == null || !StringUtils.hasText(dto.getName())) {
                skipped++;
                continue;
            }
            Faculty faculty = null;
            if (dto.getParent() != null) {
                faculty = facultyRepository.findByHemisId(dto.getParent()).orElse(null);
            }

            Department department = departmentRepository.findByHemisId(dto.getId()).orElse(null);
            if (department == null) {
                department = Department.builder()
                        .name(dto.getName())
                        .hemisId(dto.getId())
                        .code(dto.getCode())
                        .hemisActive(Boolean.TRUE.equals(dto.getActive()))
                        .structureTypeCode(codeOf(dto))
                        .parentHemisId(dto.getParent())
                        .faculty(faculty)
                        .build();
                department.setCreatedUsername(SecurityUtils.getCurrentUsername());
                departmentRepository.save(department);
                created++;
            } else {
                department.setName(dto.getName());
                department.setCode(dto.getCode());
                department.setHemisActive(Boolean.TRUE.equals(dto.getActive()));
                department.setStructureTypeCode(codeOf(dto));
                department.setParentHemisId(dto.getParent());
                if (faculty != null) {
                    department.setFaculty(faculty);
                }
                departmentRepository.save(department);
                updated++;
            }
        }

        return result(items.size(), created, updated, skipped);
    }

    @Override
    @Transactional(readOnly = true)
    public HemisEmployeeListResponse fetchEmployees(HemisEmployeeQuery query) {
        return hemisClient.fetchEmployees(normalizeEmployees(query, false));
    }

    @Override
    public HemisSyncResult syncTeachers(HemisEmployeeQuery query) {
        List<HemisEmployeeDto> items = hemisClient.fetchEmployees(normalizeEmployees(query, true)).getItems();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (HemisEmployeeDto dto : items) {
            if (dto.getId() == null || !hasEmployeeName(dto)) {
                skipped++;
                continue;
            }

            Department department = null;
            Long departmentHemisId = dto.getDepartment() != null ? dto.getDepartment().getId() : null;
            if (departmentHemisId != null) {
                department = departmentRepository.findByHemisId(departmentHemisId).orElse(null);
            }

            Teacher teacher = teacherRepository.findByHemisId(dto.getId()).orElse(null);
            if (teacher == null) {
                teacher = Teacher.builder().hemisId(dto.getId()).build();
                teacher.setCreatedUsername(SecurityUtils.getCurrentUsername());
                applyEmployee(teacher, dto, department, departmentHemisId);
                teacherRepository.save(teacher);
                created++;
            } else {
                applyEmployee(teacher, dto, department, departmentHemisId);
                teacherRepository.save(teacher);
                updated++;
            }
        }

        return result(items.size(), created, updated, skipped);
    }

    private void applyEmployee(
            Teacher teacher,
            HemisEmployeeDto dto,
            Department department,
            Long departmentHemisId
    ) {
        teacher.setFirstName(dto.getFirstName());
        teacher.setSecondName(dto.getSecondName());
        teacher.setThirdName(dto.getThirdName());
        teacher.setFullName(resolveFullName(dto));
        teacher.setShortName(dto.getShortName());
        teacher.setEmployeeIdNumber(dto.getEmployeeIdNumber());
        teacher.setBirthDate(dto.getBirthDate());
        teacher.setImage(dto.getImage());
        teacher.setGenderCode(codeOf(dto.getGender()));
        teacher.setGenderName(nameOf(dto.getGender()));
        teacher.setDepartmentHemisId(departmentHemisId);
        if (department != null) {
            teacher.setDepartment(department);
        }
        teacher.setStaffPositionCode(codeOf(dto.getStaffPosition()));
        teacher.setStaffPositionName(nameOf(dto.getStaffPosition()));
        teacher.setEmployeeStatusCode(codeOf(dto.getEmployeeStatus()));
        teacher.setEmployeeStatusName(nameOf(dto.getEmployeeStatus()));
        teacher.setEmploymentFormCode(codeOf(dto.getEmploymentForm()));
        teacher.setEmploymentFormName(nameOf(dto.getEmploymentForm()));
        teacher.setEmploymentStaffCode(codeOf(dto.getEmploymentStaff()));
        teacher.setEmploymentStaffName(nameOf(dto.getEmploymentStaff()));
        teacher.setEmployeeTypeCode(codeOf(dto.getEmployeeType()));
        teacher.setEmployeeTypeName(nameOf(dto.getEmployeeType()));
        teacher.setAcademicRankCode(codeOf(dto.getAcademicRank()));
        teacher.setAcademicRankName(nameOf(dto.getAcademicRank()));
        teacher.setAcademicDegreeCode(codeOf(dto.getAcademicDegree()));
        teacher.setAcademicDegreeName(nameOf(dto.getAcademicDegree()));
    }

    private boolean hasEmployeeName(HemisEmployeeDto dto) {
        return StringUtils.hasText(dto.getFullName())
                || StringUtils.hasText(dto.getFirstName())
                || StringUtils.hasText(dto.getSecondName());
    }

    private String resolveFullName(HemisEmployeeDto dto) {
        if (StringUtils.hasText(dto.getFullName())) {
            return dto.getFullName();
        }
        return Stream.of(dto.getSecondName(), dto.getFirstName(), dto.getThirdName())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
    }

    private HemisDepartmentQuery normalize(HemisDepartmentQuery query, boolean forSync) {
        HemisDepartmentQuery q = query == null ? new HemisDepartmentQuery() : query;
        if (q.getPage() == null || q.getPage() < 1) {
            q.setPage(1);
        }
        if (q.getLimit() == null || q.getLimit() < 1) {
            q.setLimit(50);
        }
        if (!StringUtils.hasText(q.getActive())) {
            q.setActive("1");
        }
        if (forSync) {
            q.setFetchAllPages(true);
        }
        return q;
    }

    private HemisEmployeeQuery normalizeEmployees(HemisEmployeeQuery query, boolean forSync) {
        HemisEmployeeQuery q = query == null ? new HemisEmployeeQuery() : query;
        if (q.getPage() == null || q.getPage() < 1) {
            q.setPage(1);
        }
        if (q.getLimit() == null || q.getLimit() < 1) {
            q.setLimit(50);
        }
        if (!StringUtils.hasText(q.getType())) {
            q.setType("teacher");
        }
        if (forSync) {
            q.setFetchAllPages(true);
        }
        return q;
    }

    private String codeOf(HemisDepartmentDto dto) {
        return dto.getStructureType() != null ? dto.getStructureType().getCode() : null;
    }

    private String codeOf(HemisEmployeeDto.Classifier classifier) {
        return classifier != null ? classifier.getCode() : null;
    }

    private String nameOf(HemisEmployeeDto.Classifier classifier) {
        return classifier != null ? classifier.getName() : null;
    }

    private HemisSyncResult result(int fetched, int created, int updated, int skipped) {
        return HemisSyncResult.builder()
                .fetched(fetched)
                .created(created)
                .updated(updated)
                .skipped(skipped)
                .build();
    }
}
