package uz.urspi.allocate.direction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.direction.dto.DirectionRequest;
import uz.urspi.allocate.direction.entity.Direction;
import uz.urspi.allocate.direction.repository.DirectionRepository;
import uz.urspi.allocate.direction.response.DirectionResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DirectionServiceImpl implements DirectionService {

    private final DirectionRepository directionRepository;

    @Override
    @Auditable(entity = "Direction", action = AuditAction.CREATE)
    public DirectionResponse create(DirectionRequest request) {
        validateUniqueCode(request.getDirectionCode(), null);
        Direction direction = Direction.builder()
                .directionCode(normalize(request.getDirectionCode()))
                .directionName(request.getDirectionName().trim())
                .build();
        direction.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return toResponse(directionRepository.save(direction));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirectionResponse> findAll() {
        return directionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DirectionResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Auditable(entity = "Direction", action = AuditAction.UPDATE)
    public DirectionResponse update(Long id, DirectionRequest request) {
        validateUniqueCode(request.getDirectionCode(), id);
        Direction direction = getOrThrow(id);
        direction.setDirectionCode(normalize(request.getDirectionCode()));
        direction.setDirectionName(request.getDirectionName().trim());
        return toResponse(directionRepository.save(direction));
    }

    @Override
    @Auditable(entity = "Direction", action = AuditAction.DELETE)
    public void delete(Long id) {
        Direction direction = getOrThrow(id);
        direction.softDelete();
        directionRepository.save(direction);
    }

    private void validateUniqueCode(String code, Long currentId) {
        String normalized = normalize(code);
        boolean exists = currentId == null
                ? directionRepository.existsByDirectionCodeIgnoreCase(normalized)
                : directionRepository.existsByDirectionCodeIgnoreCaseAndIdNot(normalized, currentId);
        if (exists) {
            throw new BadRequestException("Bunday yo'nalish kodi allaqachon mavjud");
        }
    }

    private Direction getOrThrow(Long id) {
        return directionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Direction", id));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private DirectionResponse toResponse(Direction direction) {
        return DirectionResponse.builder()
                .id(direction.getId())
                .name(direction.getDirectionName())
                .directionCode(direction.getDirectionCode())
                .directionName(direction.getDirectionName())
                .hemisId(direction.getHemisId())
                .hemisActive(direction.getHemisActive())
                .status(direction.getStatus())
                .build();
    }
}
