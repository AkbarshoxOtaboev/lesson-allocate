package uz.urspi.allocate.audit.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.common.enums.AuditAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private String username;

    @JsonProperty("entity")
    private String entityType;

    private AuditAction action;
    private String httpMethod;
    private String url;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
