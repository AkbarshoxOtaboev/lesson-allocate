package uz.urspi.allocate.teacher.service;

import uz.urspi.allocate.teacher.dto.TeacherRequest;
import uz.urspi.allocate.teacher.response.TeacherResponse;

import java.util.List;

public interface TeacherService {

    TeacherResponse create(TeacherRequest request);

    List<TeacherResponse> findAll(Long facultyId, Long departmentId);

    TeacherResponse findById(Long id);

    TeacherResponse update(Long id, TeacherRequest request);

    void delete(Long id);
}
