package uz.urspi.allocate.academicyear.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.academicyear.dto.AcademicYearRequest;
import uz.urspi.allocate.academicyear.entity.AcademicYear;
import uz.urspi.allocate.academicyear.repository.AcademicYearRepository;
import uz.urspi.allocate.academicyear.response.AcademicYearResponse;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    @Override
    @Auditable(entity = "AcademicYear", action = AuditAction.CREATE)
    public AcademicYearResponse create(AcademicYearRequest request) {
        validateYears(request.getStartYear(), request.getEndYear());
        String name = buildName(request.getStartYear(), request.getEndYear());
        if (academicYearRepository.existsByName(name)) {
            throw new BadRequestException("Bu o'quv yili allaqachon mavjud: " + name);
        }

        boolean makeCurrent = Boolean.TRUE.equals(request.getCurrentYear());
        if (makeCurrent) {
            academicYearRepository.clearCurrentYear();
        }

        AcademicYear entity = AcademicYear.builder()
                .name(name)
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .currentYear(makeCurrent)
                .build();
        entity.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(academicYearRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicYearResponse> findAll() {
        return academicYearRepository.findAll().stream()
                .sorted(Comparator.comparing(AcademicYear::getStartYear).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Auditable(entity = "AcademicYear", action = AuditAction.UPDATE)
    public AcademicYearResponse update(Long id, AcademicYearRequest request) {
        validateYears(request.getStartYear(), request.getEndYear());
        AcademicYear entity = getOrThrow(id);
        String name = buildName(request.getStartYear(), request.getEndYear());

        academicYearRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Bu o'quv yili allaqachon mavjud: " + name);
            }
        });

        boolean makeCurrent = Boolean.TRUE.equals(request.getCurrentYear());
        if (makeCurrent && !Boolean.TRUE.equals(entity.getCurrentYear())) {
            academicYearRepository.clearCurrentYear();
        }

        entity.setName(name);
        entity.setStartYear(request.getStartYear());
        entity.setEndYear(request.getEndYear());
        entity.setCurrentYear(makeCurrent);
        return toResponse(academicYearRepository.save(entity));
    }

    @Override
    @Auditable(entity = "AcademicYear", action = AuditAction.DELETE)
    public void delete(Long id) {
        AcademicYear entity = getOrThrow(id);
        entity.softDelete();
        academicYearRepository.save(entity);
    }

    private void validateYears(Integer startYear, Integer endYear) {
        if (startYear == null || endYear == null) {
            throw new BadRequestException("Boshlanish va tugash yili majburiy");
        }
        if (endYear != startYear + 1) {
            throw new BadRequestException("Tugash yili boshlanish yilidan 1 ga katta bo'lishi kerak (masalan 2025-2026)");
        }
    }

    private String buildName(Integer startYear, Integer endYear) {
        return startYear + "-" + endYear;
    }

    private AcademicYear getOrThrow(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("AcademicYear", id));
    }

    private AcademicYearResponse toResponse(AcademicYear entity) {
        return AcademicYearResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .startYear(entity.getStartYear())
                .endYear(entity.getEndYear())
                .currentYear(Boolean.TRUE.equals(entity.getCurrentYear()))
                .status(entity.getStatus())
                .build();
    }
}
