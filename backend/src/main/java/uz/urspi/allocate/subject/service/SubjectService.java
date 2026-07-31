package uz.urspi.allocate.subject.service;

import uz.urspi.allocate.subject.dto.SubjectRequest;
import uz.urspi.allocate.subject.enums.EducationType;
import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.subject.response.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse create(SubjectRequest request);

    List<SubjectResponse> findAll(
            Long facultyId,
            Long departmentId,
            Semester semester,
            Integer courseYear,
            Long directionId,
            EducationType educationType
    );

    SubjectResponse findById(Long id);

    SubjectResponse update(Long id, SubjectRequest request);

    void delete(Long id);
}
