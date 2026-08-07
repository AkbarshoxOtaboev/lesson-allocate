package uz.urspi.allocate.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.group.dto.NameRequest;
import uz.urspi.allocate.group.entity.Group;
import uz.urspi.allocate.group.repository.GroupRepository;
import uz.urspi.allocate.group.response.GroupResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Auditable(entity = "Group", action = AuditAction.CREATE)
    public GroupResponse create(NameRequest request) {
        Group group = Group.builder()
                .name(request.getName())
                .studentCount(request.getStudentCount() != null ? request.getStudentCount() : 0)
                .department(resolveDepartment(request.getDepartmentId()))
                .build();
        group.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(groupRepository.save(group));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> findAll(Long departmentId, Long facultyId) {
        List<Group> groups;
        if (departmentId != null) {
            groups = groupRepository.findByDepartment_Id(departmentId);
        } else if (facultyId != null) {
            groups = groupRepository.findByFaculty_Id(facultyId);
        } else {
            groups = groupRepository.findAll();
        }
        return groups.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Auditable(entity = "Group", action = AuditAction.UPDATE)
    public GroupResponse update(Long id, NameRequest request) {
        Group group = getOrThrow(id);
        group.setName(request.getName());
        if (request.getDepartmentId() != null) {
            group.setDepartment(resolveDepartment(request.getDepartmentId()));
        }
        if (request.getStudentCount() != null) {
            group.setStudentCount(Math.max(0, request.getStudentCount()));
        }
        return toResponse(groupRepository.save(group));
    }

    @Override
    @Auditable(entity = "Group", action = AuditAction.DELETE)
    public void delete(Long id) {
        Group group = getOrThrow(id);
        group.softDelete();
        groupRepository.save(group);
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", departmentId));
    }

    private Group getOrThrow(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Group", id));
    }

    private GroupResponse toResponse(Group group) {
        Department department = group.getDepartment();
        Faculty faculty = group.getFaculty();
        if (faculty == null && department != null) {
            faculty = department.getFaculty();
        }
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .studentCount(group.getStudentCount() != null ? group.getStudentCount() : 0)
                .status(group.getStatus())
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : group.getHemisDepartmentName())
                .facultyId(faculty != null ? faculty.getId() : null)
                .facultyName(faculty != null ? faculty.getName() : null)
                .hemisDepartmentName(group.getHemisDepartmentName())
                .hemisId(group.getHemisId())
                .hemisActive(group.getHemisActive())
                .curriculumHemisId(group.getCurriculumHemisId())
                .curriculumName(group.getCurriculumName())
                .specialtyHemisId(group.getSpecialtyHemisId())
                .specialtyName(group.getSpecialtyName())
                .educationTypeCode(group.getEducationTypeCode())
                .educationTypeName(group.getEducationTypeName())
                .educationFormCode(group.getEducationFormCode())
                .educationFormName(group.getEducationFormName())
                .educationLangCode(group.getEducationLangCode())
                .educationLangName(group.getEducationLangName())
                .build();
    }
}
