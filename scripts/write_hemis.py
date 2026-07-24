from pathlib import Path

ROOT = Path(r"e:\vibe\lesson-allocate\backend\src\main\java\uz\urspi\allocate")


def w(rel: str, content: str) -> None:
    path = ROOT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")
    print("wrote", path)


w(
    "config/PermissionsDataLoader.java",
    """
package uz.urspi.allocate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uz.urspi.allocate.common.enums.ActionType;
import uz.urspi.allocate.common.enums.ResourceType;
import uz.urspi.allocate.permission.entity.Permission;
import uz.urspi.allocate.permission.repository.PermissionRepository;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class PermissionsDataLoader implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        int created = 0;
        for (ResourceType resource : ResourceType.values()) {
            for (ActionType action : ActionType.values()) {
                String name = resource.name() + "_" + action.name();
                if (!permissionRepository.existsByName(name)) {
                    Permission permission = Permission.builder().name(name).build();
                    permission.setCreatedUsername("system");
                    permissionRepository.save(permission);
                    created++;
                }
            }
        }
        log.info("PermissionsDataLoader: {} new permissions created", created);
    }
}
""",
)

w(
    "config/HemisProperties.java",
    """
package uz.urspi.allocate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.hemis")
public class HemisProperties {

    private String baseUrl = "https://student.urspi.uz/rest";

    private String language = "uz-UZ";

    private int pageSize = 200;
}
""",
)

app_props = ROOT / "config/AppProperties.java"
text = app_props.read_text(encoding="utf-8")
if "HemisProperties" not in text:
    text = text.replace(
        "StorageProperties.class\n})",
        "StorageProperties.class,\n        HemisProperties.class\n})",
    )
    app_props.write_text(text, encoding="utf-8")
    print("patched AppProperties")

w(
    "hemis/entity/ExternalToken.java",
    """
package uz.urspi.allocate.hemis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.urspi.allocate.common.entity.BaseEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "external_tokens", uniqueConstraints = @UniqueConstraint(columnNames = "provider"))
@SQLRestriction("status <> 'DELETED'")
public class ExternalToken extends BaseEntity {

    public static final String PROVIDER_HEMIS = "HEMIS";

    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 64)
    private String provider;

    @Lob
    @Column(nullable = false)
    private String accessToken;

    private String baseUrl;

    private String description;
}
""",
)

w(
    "hemis/repository/ExternalTokenRepository.java",
    """
package uz.urspi.allocate.hemis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.hemis.entity.ExternalToken;

import java.util.Optional;

public interface ExternalTokenRepository extends JpaRepository<ExternalToken, Long> {

    Optional<ExternalToken> findByProvider(String provider);

    boolean existsByProvider(String provider);
}
""",
)

w(
    "hemis/dto/HemisTokenRequest.java",
    """
package uz.urspi.allocate.hemis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HemisTokenRequest {

    @NotBlank
    private String accessToken;

    private String baseUrl;

    private String description;
}
""",
)

w(
    "hemis/response/HemisTokenResponse.java",
    """
package uz.urspi.allocate.hemis.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HemisTokenResponse {

    private Long id;
    private String provider;
    private String maskedToken;
    private boolean configured;
    private String baseUrl;
    private String description;
    private LocalDateTime updatedAt;
}
""",
)

w(
    "hemis/dto/HemisDepartmentDto.java",
    """
package uz.urspi.allocate.hemis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HemisDepartmentDto {

    private Long id;
    private String name;
    private String code;
    private Long parent;
    private Boolean active;
    private Classifier structureType;
    private Classifier localityType;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Classifier {
        private String code;
        private String name;
    }
}
""",
)

w(
    "hemis/response/HemisSyncResult.java",
    """
package uz.urspi.allocate.hemis.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HemisSyncResult {

    private int fetched;
    private int created;
    private int updated;
    private int skipped;
}
""",
)

w(
    "hemis/client/HemisClient.java",
    """
package uz.urspi.allocate.hemis.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import uz.urspi.allocate.common.exception.ApiException;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.config.HemisProperties;
import uz.urspi.allocate.hemis.dto.HemisDepartmentDto;
import uz.urspi.allocate.hemis.entity.ExternalToken;
import uz.urspi.allocate.hemis.repository.ExternalTokenRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HemisClient {

    private final ExternalTokenRepository externalTokenRepository;
    private final HemisProperties hemisProperties;
    private final ObjectMapper objectMapper;

    public List<HemisDepartmentDto> fetchDepartments() {
        ExternalToken token = requireToken();
        String baseUrl = resolveBaseUrl(token);

        List<HemisDepartmentDto> all = new ArrayList<>();
        int page = 1;
        int pageSize = Math.max(1, Math.min(hemisProperties.getPageSize(), 200));

        while (true) {
            String uri = baseUrl + "/v1/data/department-list"
                    + "?page=" + page
                    + "&limit=" + pageSize
                    + "&active=1"
                    + "&l=" + hemisProperties.getLanguage();

            try {
                String body = RestClient.create()
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(body);
                if (root.has("success") && !root.get("success").asBoolean(true)) {
                    throw new ApiException("HEMIS error: " + root.path("error").asText("unknown"),
                            HttpStatus.BAD_GATEWAY);
                }

                JsonNode data = root.path("data");
                JsonNode itemsNode;
                if (data.isArray() && !data.isEmpty()) {
                    itemsNode = data.get(0).path("items");
                } else if (data.has("items")) {
                    itemsNode = data.path("items");
                } else {
                    itemsNode = data;
                }

                List<HemisDepartmentDto> pageItems = objectMapper.convertValue(
                        itemsNode,
                        new TypeReference<List<HemisDepartmentDto>>() {}
                );
                if (pageItems == null || pageItems.isEmpty()) {
                    break;
                }
                all.addAll(pageItems);

                int pageCount = 1;
                JsonNode pagination = data.isArray() && !data.isEmpty()
                        ? data.get(0).path("pagination")
                        : data.path("pagination");
                if (pagination.isArray() && !pagination.isEmpty()) {
                    pageCount = pagination.get(0).path("pageCount").asInt(1);
                } else if (pagination.isObject()) {
                    pageCount = pagination.path("pageCount").asInt(1);
                }

                if (page >= pageCount || pageItems.size() < pageSize) {
                    break;
                }
                page++;
            } catch (RestClientResponseException ex) {
                log.error("HEMIS department-list failed: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                throw new ApiException("HEMIS so'rovi muvaffaqiyatsiz: " + ex.getStatusCode().value(),
                        HttpStatus.BAD_GATEWAY);
            } catch (ApiException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("HEMIS parse error", ex);
                throw new ApiException("HEMIS javobini o'qib bo'lmadi: " + ex.getMessage(),
                        HttpStatus.BAD_GATEWAY);
            }
        }
        return all;
    }

    private ExternalToken requireToken() {
        return externalTokenRepository.findByProvider(ExternalToken.PROVIDER_HEMIS)
                .orElseThrow(() -> new BadRequestException(
                        "HEMIS token saqlanmagan. Avval /api/hemis/token orqali token kiriting."));
    }

    private String resolveBaseUrl(ExternalToken token) {
        String url = token.getBaseUrl() != null && !token.getBaseUrl().isBlank()
                ? token.getBaseUrl()
                : hemisProperties.getBaseUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
""",
)

w(
    "hemis/service/HemisTokenService.java",
    """
package uz.urspi.allocate.hemis.service;

import uz.urspi.allocate.hemis.dto.HemisTokenRequest;
import uz.urspi.allocate.hemis.response.HemisTokenResponse;

public interface HemisTokenService {

    HemisTokenResponse getTokenInfo();

    HemisTokenResponse saveToken(HemisTokenRequest request);

    void deleteToken();
}
""",
)

w(
    "hemis/service/HemisTokenServiceImpl.java",
    """
package uz.urspi.allocate.hemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.config.HemisProperties;
import uz.urspi.allocate.hemis.dto.HemisTokenRequest;
import uz.urspi.allocate.hemis.entity.ExternalToken;
import uz.urspi.allocate.hemis.repository.ExternalTokenRepository;
import uz.urspi.allocate.hemis.response.HemisTokenResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class HemisTokenServiceImpl implements HemisTokenService {

    private final ExternalTokenRepository externalTokenRepository;
    private final HemisProperties hemisProperties;

    @Override
    @Transactional(readOnly = true)
    public HemisTokenResponse getTokenInfo() {
        return externalTokenRepository.findByProvider(ExternalToken.PROVIDER_HEMIS)
                .map(this::toResponse)
                .orElseGet(() -> HemisTokenResponse.builder()
                        .provider(ExternalToken.PROVIDER_HEMIS)
                        .configured(false)
                        .baseUrl(hemisProperties.getBaseUrl())
                        .build());
    }

    @Override
    public HemisTokenResponse saveToken(HemisTokenRequest request) {
        ExternalToken token = externalTokenRepository.findByProvider(ExternalToken.PROVIDER_HEMIS)
                .orElseGet(() -> ExternalToken.builder()
                        .provider(ExternalToken.PROVIDER_HEMIS)
                        .build());

        token.setAccessToken(request.getAccessToken().trim());
        if (StringUtils.hasText(request.getBaseUrl())) {
            token.setBaseUrl(request.getBaseUrl().trim());
        } else if (token.getBaseUrl() == null) {
            token.setBaseUrl(hemisProperties.getBaseUrl());
        }
        if (request.getDescription() != null) {
            token.setDescription(request.getDescription());
        }
        if (token.getId() == null) {
            token.setCreatedUsername(SecurityUtils.getCurrentUsername());
        }
        return toResponse(externalTokenRepository.save(token));
    }

    @Override
    public void deleteToken() {
        externalTokenRepository.findByProvider(ExternalToken.PROVIDER_HEMIS)
                .ifPresent(token -> {
                    token.softDelete();
                    externalTokenRepository.save(token);
                });
    }

    private HemisTokenResponse toResponse(ExternalToken token) {
        return HemisTokenResponse.builder()
                .id(token.getId())
                .provider(token.getProvider())
                .maskedToken(mask(token.getAccessToken()))
                .configured(StringUtils.hasText(token.getAccessToken()))
                .baseUrl(token.getBaseUrl() != null ? token.getBaseUrl() : hemisProperties.getBaseUrl())
                .description(token.getDescription())
                .updatedAt(token.getUpdatedAt())
                .build();
    }

    private String mask(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}
""",
)

w(
    "hemis/service/HemisSyncService.java",
    """
package uz.urspi.allocate.hemis.service;

import uz.urspi.allocate.hemis.dto.HemisDepartmentDto;
import uz.urspi.allocate.hemis.response.HemisSyncResult;

import java.util.List;

public interface HemisSyncService {

    List<HemisDepartmentDto> fetchDepartments();

    HemisSyncResult syncFaculties();
}
""",
)

w(
    "hemis/service/HemisSyncServiceImpl.java",
    """
package uz.urspi.allocate.hemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.faculty.repository.FacultyRepository;
import uz.urspi.allocate.hemis.client.HemisClient;
import uz.urspi.allocate.hemis.dto.HemisDepartmentDto;
import uz.urspi.allocate.hemis.response.HemisSyncResult;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HemisSyncServiceImpl implements HemisSyncService {

    private final HemisClient hemisClient;
    private final FacultyRepository facultyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HemisDepartmentDto> fetchDepartments() {
        return hemisClient.fetchDepartments();
    }

    @Override
    public HemisSyncResult syncFaculties() {
        List<HemisDepartmentDto> departments = hemisClient.fetchDepartments();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (HemisDepartmentDto dto : departments) {
            if (dto.getId() == null || !StringUtils.hasText(dto.getName())) {
                skipped++;
                continue;
            }

            Faculty faculty = facultyRepository.findByHemisId(dto.getId()).orElse(null);
            if (faculty == null) {
                faculty = Faculty.builder()
                        .name(dto.getName())
                        .hemisId(dto.getId())
                        .code(dto.getCode())
                        .hemisActive(Boolean.TRUE.equals(dto.getActive()))
                        .structureTypeCode(dto.getStructureType() != null ? dto.getStructureType().getCode() : null)
                        .parentHemisId(dto.getParent())
                        .build();
                faculty.setCreatedUsername(SecurityUtils.getCurrentUsername());
                facultyRepository.save(faculty);
                created++;
            } else {
                faculty.setName(dto.getName());
                faculty.setCode(dto.getCode());
                faculty.setHemisActive(Boolean.TRUE.equals(dto.getActive()));
                faculty.setStructureTypeCode(dto.getStructureType() != null ? dto.getStructureType().getCode() : null);
                faculty.setParentHemisId(dto.getParent());
                facultyRepository.save(faculty);
                updated++;
            }
        }

        return HemisSyncResult.builder()
                .fetched(departments.size())
                .created(created)
                .updated(updated)
                .skipped(skipped)
                .build();
    }
}
""",
)

w(
    "hemis/controller/HemisController.java",
    """
package uz.urspi.allocate.hemis.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.urspi.allocate.hemis.dto.HemisDepartmentDto;
import uz.urspi.allocate.hemis.dto.HemisTokenRequest;
import uz.urspi.allocate.hemis.response.HemisSyncResult;
import uz.urspi.allocate.hemis.response.HemisTokenResponse;
import uz.urspi.allocate.hemis.service.HemisSyncService;
import uz.urspi.allocate.hemis.service.HemisTokenService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hemis")
public class HemisController {

    private final HemisTokenService hemisTokenService;
    private final HemisSyncService hemisSyncService;

    @GetMapping("/token")
    @PreAuthorize("hasAuthority('EXTERNAL_TOKEN_VIEW')")
    public ResponseEntity<HemisTokenResponse> getToken() {
        return ResponseEntity.ok(hemisTokenService.getTokenInfo());
    }

    @PutMapping("/token")
    @PreAuthorize("hasAnyAuthority('EXTERNAL_TOKEN_CREATE','EXTERNAL_TOKEN_EDIT')")
    public ResponseEntity<HemisTokenResponse> saveToken(@Valid @RequestBody HemisTokenRequest request) {
        return ResponseEntity.ok(hemisTokenService.saveToken(request));
    }

    @DeleteMapping("/token")
    @PreAuthorize("hasAuthority('EXTERNAL_TOKEN_DELETE')")
    public ResponseEntity<Void> deleteToken() {
        hemisTokenService.deleteToken();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyAuthority('EXTERNAL_TOKEN_VIEW','FACULTY_VIEW')")
    public ResponseEntity<List<HemisDepartmentDto>> departments() {
        return ResponseEntity.ok(hemisSyncService.fetchDepartments());
    }

    @PostMapping("/sync/faculties")
    @PreAuthorize("hasAnyAuthority('FACULTY_CREATE','FACULTY_EDIT','EXTERNAL_TOKEN_EDIT')")
    public ResponseEntity<HemisSyncResult> syncFaculties() {
        return ResponseEntity.ok(hemisSyncService.syncFaculties());
    }
}
""",
)

print("done")
