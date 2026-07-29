package uz.urspi.allocate.department.service;

import uz.urspi.allocate.department.dto.NameRequest;
import uz.urspi.allocate.department.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(NameRequest request);

    List<DepartmentResponse> findAll(Long facultyId);

    /** Fakultet scope'siz — talabnoma yaratishda barcha kafedralar */
    List<DepartmentResponse> findAllForTalabnoma();

    DepartmentResponse findById(Long id);

    DepartmentResponse update(Long id, NameRequest request);

    void delete(Long id);
}
