package uz.urspi.allocate.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.audit.entity.AuditLog;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
