package uz.urspi.allocate.subject.service;

import uz.urspi.allocate.subject.dto.SubjectRequest;
import uz.urspi.allocate.subject.response.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse create(SubjectRequest request);

    List<SubjectResponse> findAll(Long facultyId, Long departmentId);

    SubjectResponse findById(Long id);

    SubjectResponse update(Long id, SubjectRequest request);

    void delete(Long id);
}
