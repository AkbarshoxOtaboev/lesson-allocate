package uz.urspi.allocate.hemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.direction.entity.Direction;
import uz.urspi.allocate.direction.repository.DirectionRepository;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.faculty.repository.FacultyRepository;
import uz.urspi.allocate.hemis.client.HemisClient;
import uz.urspi.allocate.hemis.dto.HemisDepartmentDto;
import uz.urspi.allocate.hemis.dto.HemisDepartmentQuery;
import uz.urspi.allocate.hemis.dto.HemisEmployeeDto;
import uz.urspi.allocate.hemis.dto.HemisEmployeeQuery;
import uz.urspi.allocate.hemis.dto.HemisGroupDto;
import uz.urspi.allocate.hemis.dto.HemisGroupQuery;
import uz.urspi.allocate.hemis.dto.HemisSpecialtyDto;
import uz.urspi.allocate.hemis.dto.HemisSpecialtyQuery;
import uz.urspi.allocate.hemis.response.HemisDepartmentListResponse;
import uz.urspi.allocate.hemis.response.HemisEmployeeListResponse;
import uz.urspi.allocate.hemis.response.HemisGroupListResponse;
import uz.urspi.allocate.hemis.response.HemisSpecialtyListResponse;
import uz.urspi.allocate.hemis.response.HemisSyncResult;
import uz.urspi.allocate.group.entity.Group;
import uz.urspi.allocate.group.repository.GroupRepository;
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
    private final GroupRepository groupRepository;
    private final DirectionRepository directionRepository;

    @Override
    @Transactional(readOnly = true)
    public HemisDepartmentListResponse fetchDepartments(HemisDepartmentQuery query) {
        return hemisClient.fetchDepartments(normalize(query, false));
    }

    @Override
    @Auditable(entity = "HemisSync", action = AuditAction.UPDATE)
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
    @Auditable(entity = "HemisSync", action = AuditAction.UPDATE)
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
    @Auditable(entity = "HemisSync", action = AuditAction.UPDATE)
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

    @Override
    @Transactional(readOnly = true)
    public HemisGroupListResponse fetchGroups(HemisGroupQuery query) {
        return hemisClient.fetchGroups(normalizeGroups(query, false));
    }

    @Override
    @Auditable(entity = "HemisSync", action = AuditAction.UPDATE)
    public HemisSyncResult syncGroups(HemisGroupQuery query) {
        List<HemisGroupDto> items = hemisClient.fetchGroups(normalizeGroups(query, true)).getItems();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (HemisGroupDto dto : items) {
            if (dto.getId() == null || !StringUtils.hasText(dto.getName())) {
                skipped++;
                continue;
            }

            Department department = null;
            Faculty faculty = null;
            Long hemisOrgId = dto.getDepartment() != null ? dto.getDepartment().getId() : null;
            if (hemisOrgId != null) {
                department = departmentRepository.findByHemisId(hemisOrgId).orElse(null);
                if (department != null) {
                    faculty = department.getFaculty();
                } else {
                    // HEMIS group-list often returns faculty under "department"
                    faculty = facultyRepository.findByHemisId(hemisOrgId).orElse(null);
                }
            }

            Group group = groupRepository.findByHemisId(dto.getId()).orElse(null);
            if (group == null) {
                group = Group.builder()
                        .name(dto.getName())
                        .hemisId(dto.getId())
                        .build();
                group.setCreatedUsername(SecurityUtils.getCurrentUsername());
                applyGroup(group, dto, department, faculty, hemisOrgId);
                groupRepository.save(group);
                created++;
            } else {
                group.setName(dto.getName());
                applyGroup(group, dto, department, faculty, hemisOrgId);
                groupRepository.save(group);
                updated++;
            }
        }

        return result(items.size(), created, updated, skipped);
    }

    @Override
    @Transactional(readOnly = true)
    public HemisSpecialtyListResponse fetchSpecialties(HemisSpecialtyQuery query) {
        return hemisClient.fetchSpecialties(normalizeSpecialties(query, false));
    }

    @Override
    @Auditable(entity = "HemisSync", action = AuditAction.UPDATE)
    public HemisSyncResult syncDirections(HemisSpecialtyQuery query) {
        List<HemisSpecialtyDto> items = hemisClient.fetchSpecialties(normalizeSpecialties(query, true)).getItems();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (HemisSpecialtyDto dto : items) {
            if (dto.getId() == null || !StringUtils.hasText(dto.getName())) {
                skipped++;
                continue;
            }

            String code = resolveSpecialtyCode(dto);
            Direction direction = directionRepository.findByHemisId(dto.getId()).orElse(null);
            if (direction == null) {
                direction = directionRepository.findByDirectionCodeIgnoreCase(code).orElse(null);
            }

            if (direction == null) {
                direction = Direction.builder()
                        .directionCode(code)
                        .directionName(dto.getName().trim())
                        .hemisId(dto.getId())
                        .build();
                direction.setCreatedUsername(SecurityUtils.getCurrentUsername());
                applySpecialty(direction, dto, code);
                directionRepository.save(direction);
                created++;
            } else {
                applySpecialty(direction, dto, resolveUpdatableCode(direction, code));
                directionRepository.save(direction);
                updated++;
            }
        }

        return result(items.size(), created, updated, skipped);
    }

    private void applySpecialty(Direction direction, HemisSpecialtyDto dto, String code) {
        direction.setDirectionCode(code);
        direction.setDirectionName(dto.getName().trim());
        direction.setHemisId(dto.getId());
        direction.setHemisActive(Boolean.TRUE.equals(dto.getActive()));
        if (dto.getDepartment() != null) {
            direction.setDepartmentHemisId(dto.getDepartment().getId());
        }
        if (dto.getEducationType() != null) {
            direction.setEducationTypeCode(dto.getEducationType().getCode());
        }
        if (dto.getLocalityType() != null) {
            direction.setLocalityTypeCode(dto.getLocalityType().getCode());
        }
    }

    private String resolveSpecialtyCode(HemisSpecialtyDto dto) {
        if (StringUtils.hasText(dto.getCode())) {
            return dto.getCode().trim();
        }
        return "HEMIS-" + dto.getId();
    }

    private String resolveUpdatableCode(Direction existing, String preferredCode) {
        if (preferredCode.equalsIgnoreCase(existing.getDirectionCode())) {
            return existing.getDirectionCode();
        }
        boolean taken = directionRepository.existsByDirectionCodeIgnoreCaseAndIdNot(
                preferredCode, existing.getId());
        if (taken) {
            return existing.getDirectionCode();
        }
        return preferredCode;
    }

    private void applyGroup(
            Group group,
            HemisGroupDto dto,
            Department department,
            Faculty faculty,
            Long hemisOrgId
    ) {
        group.setHemisActive(Boolean.TRUE.equals(dto.getActive()));
        group.setDepartmentHemisId(hemisOrgId);
        if (dto.getDepartment() != null && StringUtils.hasText(dto.getDepartment().getName())) {
            group.setHemisDepartmentName(dto.getDepartment().getName());
        }
        if (department != null) {
            group.setDepartment(department);
            if (faculty == null) {
                faculty = department.getFaculty();
            }
        }
        if (faculty != null) {
            group.setFaculty(faculty);
            group.setFacultyHemisId(faculty.getHemisId());
        } else if (hemisOrgId != null && department == null) {
            group.setFacultyHemisId(hemisOrgId);
        }
        if (dto.getCurriculumId() != null) {
            group.setCurriculumHemisId(dto.getCurriculumId());
        }
        if (dto.getSpecialty() != null) {
            group.setSpecialtyHemisId(dto.getSpecialty().getId());
            group.setSpecialtyName(dto.getSpecialty().getName());
        }
        if (dto.getEducationLang() != null) {
            group.setEducationLangCode(dto.getEducationLang().getCode());
            group.setEducationLangName(dto.getEducationLang().getName());
        }
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

    private HemisGroupQuery normalizeGroups(HemisGroupQuery query, boolean forSync) {
        HemisGroupQuery q = query == null ? new HemisGroupQuery() : query;
        if (q.getPage() == null || q.getPage() < 1) {
            q.setPage(1);
        }
        if (q.getLimit() == null || q.getLimit() < 1) {
            q.setLimit(50);
        }
        if (forSync) {
            q.setFetchAllPages(true);
        }
        return q;
    }

    private HemisSpecialtyQuery normalizeSpecialties(HemisSpecialtyQuery query, boolean forSync) {
        HemisSpecialtyQuery q = query == null ? new HemisSpecialtyQuery() : query;
        if (q.getPage() == null || q.getPage() < 1) {
            q.setPage(1);
        }
        if (q.getLimit() == null || q.getLimit() < 1) {
            q.setLimit(50);
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
