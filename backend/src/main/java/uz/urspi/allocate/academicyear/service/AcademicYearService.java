package uz.urspi.allocate.academicyear.service;

import uz.urspi.allocate.academicyear.dto.AcademicYearRequest;
import uz.urspi.allocate.academicyear.response.AcademicYearResponse;

import java.util.List;

public interface AcademicYearService {

    AcademicYearResponse create(AcademicYearRequest request);

    List<AcademicYearResponse> findAll();

    AcademicYearResponse findById(Long id);

    AcademicYearResponse update(Long id, AcademicYearRequest request);

    void delete(Long id);
}
