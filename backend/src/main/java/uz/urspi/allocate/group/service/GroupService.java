package uz.urspi.allocate.group.service;

import uz.urspi.allocate.group.dto.NameRequest;
import uz.urspi.allocate.group.response.GroupResponse;

import java.util.List;

public interface GroupService {

    GroupResponse create(NameRequest request);

    List<GroupResponse> findAll(Long departmentId, Long facultyId);

    GroupResponse findById(Long id);

    GroupResponse update(Long id, NameRequest request);

    void delete(Long id);
}
