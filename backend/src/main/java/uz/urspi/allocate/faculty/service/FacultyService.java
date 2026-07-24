package uz.urspi.allocate.faculty.service;

import uz.urspi.allocate.faculty.dto.NameRequest;
import uz.urspi.allocate.faculty.response.FacultyResponse;

import java.util.List;

public interface FacultyService {

    FacultyResponse create(NameRequest request);

    List<FacultyResponse> findAll();

    FacultyResponse findById(Long id);

    FacultyResponse update(Long id, NameRequest request);

    void delete(Long id);
}
