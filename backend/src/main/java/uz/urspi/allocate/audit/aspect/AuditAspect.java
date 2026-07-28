package uz.urspi.allocate.audit.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.audit.entity.AuditLog;
import uz.urspi.allocate.audit.service.AuditLogService;
import uz.urspi.allocate.common.util.SecurityUtils;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        try {
            return joinPoint.proceed();
        } finally {
            try {
                recordAudit(auditable);
            } catch (Exception e) {
                log.warn("Failed to record audit log", e);
            }
        }
    }

    private void recordAudit(Auditable auditable) {
        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .username(SecurityUtils.getCurrentUsername())
                .entityType(auditable.entity())
                .action(auditable.action())
                .createdAt(LocalDateTime.now());

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            builder.httpMethod(request.getMethod())
                    .url(request.getRequestURI())
                    .ipAddress(request.getRemoteAddr())
                    .userAgent(request.getHeader("User-Agent"));
        }

        auditLogService.save(builder.build());
    }
}
