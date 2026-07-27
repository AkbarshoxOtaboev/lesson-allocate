package uz.urspi.allocate.audit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.urspi.allocate.audit.entity.AuditLog;
import uz.urspi.allocate.audit.repository.AuditLogRepository;
import uz.urspi.allocate.audit.response.AuditLogResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getLogs() {
        List<AuditLogResponse> responses = auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .username(log.getUsername())
                .entityType(log.getEntityType())
                .action(log.getAction())
                .httpMethod(log.getHttpMethod())
                .url(log.getUrl())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
