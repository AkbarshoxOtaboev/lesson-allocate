package uz.urspi.allocate.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
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
    public GroupResponse create(NameRequest request) {
        Group group = Group.builder()
                .name(request.getName())
                .department(resolveDepartment(request.getDepartmentId()))
                .build();
        group.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(groupRepository.save(group));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> findAll() {
        return groupRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public GroupResponse update(Long id, NameRequest request) {
        Group group = getOrThrow(id);
        group.setName(request.getName());
        if (request.getDepartmentId() != null) {
            group.setDepartment(resolveDepartment(request.getDepartmentId()));
        }
        return toResponse(groupRepository.save(group));
    }

    @Override
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
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .status(group.getStatus())
                .departmentId(group.getDepartment() != null ? group.getDepartment().getId() : null)
                .departmentName(group.getDepartment() != null ? group.getDepartment().getName() : null)
                .build();
    }
}
