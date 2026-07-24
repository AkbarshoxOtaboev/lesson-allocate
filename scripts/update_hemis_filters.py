from pathlib import Path

ROOT = Path(r"e:\vibe\lesson-allocate\backend\src\main\java\uz\urspi\allocate")


def w(rel: str, content: str) -> None:
    path = ROOT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")
    print("wrote", rel)


w(
    "hemis/dto/HemisDepartmentQuery.java",
    """
package uz.urspi.allocate.hemis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HemisDepartmentQuery {

    private Integer page = 1;
    private Integer limit = 50;
    /** 0 | 1 | all */
    private String active = "1";
    /** HEMIS query param _structure_type */
    private String structureType;
    private Long parent;
    /** true bo'lsa barcha sahifalarni oqib sync qiladi */
    private boolean fetchAllPages;
}
""",
)

w(
    "hemis/response/HemisDepartmentListResponse.java",
    """
package uz.urspi.allocate.hemis.response;

import lombok.Builder;
import lombok.Getter;
import uz.urspi.allocate.hemis.dto.HemisDepartmentDto;

import java.util.List;

@Getter
@Builder
public class HemisDepartmentListResponse {

    private List<HemisDepartmentDto> items;
    private Integer page;
    private Integer pageCount;
    private Integer totalCount;
    private Integer pageSize;
}
""",
)

w(
    "hemis/client/HemisClient.java",
    """
package uz.urspi.allocate.hemis.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.urspi.allocate.common.exception.ApiException;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.config.HemisProperties;
import uz.urspi.allocate.hemis.dto.HemisDepartmentDto;
import uz.urspi.allocate.hemis.dto.HemisDepartmentQuery;
import uz.urspi.allocate.hemis.entity.ExternalToken;
import uz.urspi.allocate.hemis.repository.ExternalTokenRepository;
import uz.urspi.allocate.hemis.response.HemisDepartmentListResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HemisClient {

    private final ExternalTokenRepository externalTokenRepository;
    private final HemisProperties hemisProperties;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public HemisDepartmentListResponse fetchDepartments(HemisDepartmentQuery query) {
        ExternalToken token = requireToken();
        String baseUrl = resolveBaseUrl(token);

        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int limit = query.getLimit() == null ? hemisProperties.getPageSize() : query.getLimit();
        limit = Math.max(1, Math.min(limit, 200));

        if (!query.isFetchAllPages()) {
            return fetchPage(token, baseUrl, page, limit, query);
        }

        List<HemisDepartmentDto> all = new ArrayList<>();
        int current = 1;
        int pageCount = 1;
        Integer totalCount = null;
        do {
            HemisDepartmentListResponse resp = fetchPage(token, baseUrl, current, limit, query);
            if (resp.getItems() != null) {
                all.addAll(resp.getItems());
            }
            pageCount = resp.getPageCount() == null ? 1 : resp.getPageCount();
            totalCount = resp.getTotalCount();
            current++;
        } while (current <= pageCount);

        return HemisDepartmentListResponse.builder()
                .items(all)
                .page(1)
                .pageCount(pageCount)
                .pageSize(limit)
                .totalCount(totalCount != null ? totalCount : all.size())
                .build();
    }

    private HemisDepartmentListResponse fetchPage(
            ExternalToken token,
            String baseUrl,
            int page,
            int limit,
            HemisDepartmentQuery query
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(baseUrl + "/v1/data/department-list")
                .queryParam("page", page)
                .queryParam("limit", limit)
                .queryParam("l", hemisProperties.getLanguage());

        if (StringUtils.hasText(query.getActive())) {
            builder.queryParam("active", query.getActive());
        }
        if (StringUtils.hasText(query.getStructureType())) {
            builder.queryParam("_structure_type", query.getStructureType());
        }
        if (query.getParent() != null) {
            builder.queryParam("parent", query.getParent());
        }

        String uri = builder.build(true).toUriString();

        try {
            String body = RestClient.create()
                    .get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(body);
            if (root.has("success") && !root.get("success").asBoolean(true)) {
                throw new ApiException("HEMIS error: " + root.path("error").asString("unknown"),
                        HttpStatus.BAD_GATEWAY);
            }

            JsonNode data = root.path("data");
            JsonNode itemsNode;
            JsonNode paginationNode = null;
            if (data.isArray() && !data.isEmpty()) {
                itemsNode = data.get(0).path("items");
                paginationNode = data.get(0).path("pagination");
            } else if (data.has("items")) {
                itemsNode = data.path("items");
                paginationNode = data.path("pagination");
            } else {
                itemsNode = data;
            }

            List<HemisDepartmentDto> pageItems = objectList(itemsNode);
            int pageCount = 1;
            int totalCount = pageItems.size();
            int pageSize = limit;
            int currentPage = page;

            JsonNode pagination = unwrapPagination(paginationNode);
            if (pagination != null && pagination.isObject()) {
                pageCount = pagination.path("pageCount").asInt(1);
                totalCount = pagination.path("totalCount").asInt(pageItems.size());
                pageSize = pagination.path("pageSize").asInt(limit);
                currentPage = pagination.path("page").asInt(page);
            }

            return HemisDepartmentListResponse.builder()
                    .items(pageItems)
                    .page(currentPage)
                    .pageCount(pageCount)
                    .pageSize(pageSize)
                    .totalCount(totalCount)
                    .build();
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

    private JsonNode unwrapPagination(JsonNode paginationNode) {
        if (paginationNode == null || paginationNode.isNull() || paginationNode.isMissingNode()) {
            return null;
        }
        if (paginationNode.isArray() && !paginationNode.isEmpty()) {
            return paginationNode.get(0);
        }
        return paginationNode;
    }

    private List<HemisDepartmentDto> objectList(JsonNode itemsNode) {
        if (itemsNode == null || itemsNode.isNull() || itemsNode.isMissingNode()) {
            return List.of();
        }
        List<HemisDepartmentDto> list = jsonMapper.convertValue(
                itemsNode,
                new TypeReference<List<HemisDepartmentDto>>() {}
        );
        return list == null ? List.of() : list;
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
    "hemis/service/HemisSyncService.java",
    """
package uz.urspi.allocate.hemis.service;

import uz.urspi.allocate.hemis.dto.HemisDepartmentQuery;
import uz.urspi.allocate.hemis.response.HemisDepartmentListResponse;
import uz.urspi.allocate.hemis.response.HemisSyncResult;

public interface HemisSyncService {

    HemisDepartmentListResponse fetchDepartments(HemisDepartmentQuery query);

    HemisSyncResult syncFaculties(HemisDepartmentQuery query);

    HemisSyncResult syncDepartments(HemisDepartmentQuery query);
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
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.faculty.repository.FacultyRepository;
import uz.urspi.allocate.hemis.client.HemisClient;
import uz.urspi.allocate.hemis.dto.HemisDepartmentDto;
import uz.urspi.allocate.hemis.dto.HemisDepartmentQuery;
import uz.urspi.allocate.hemis.response.HemisDepartmentListResponse;
import uz.urspi.allocate.hemis.response.HemisSyncResult;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HemisSyncServiceImpl implements HemisSyncService {

    private final HemisClient hemisClient;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public HemisDepartmentListResponse fetchDepartments(HemisDepartmentQuery query) {
        return hemisClient.fetchDepartments(normalize(query, false));
    }

    @Override
    public HemisSyncResult syncFaculties(HemisDepartmentQuery query) {
        List<HemisDepartmentDto> items = hemisClient.fetchDepartments(normalize(query, true)).getItems();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (HemisDepartmentDto dto : items) {
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
                        .structureTypeCode(codeOf(dto))
                        .parentHemisId(dto.getParent())
                        .build();
                faculty.setCreatedUsername(SecurityUtils.getCurrentUsername());
                facultyRepository.save(faculty);
                created++;
            } else {
                faculty.setName(dto.getName());
                faculty.setCode(dto.getCode());
                faculty.setHemisActive(Boolean.TRUE.equals(dto.getActive()));
                faculty.setStructureTypeCode(codeOf(dto));
                faculty.setParentHemisId(dto.getParent());
                facultyRepository.save(faculty);
                updated++;
            }
        }

        return result(items.size(), created, updated, skipped);
    }

    @Override
    public HemisSyncResult syncDepartments(HemisDepartmentQuery query) {
        List<HemisDepartmentDto> items = hemisClient.fetchDepartments(normalize(query, true)).getItems();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (HemisDepartmentDto dto : items) {
            if (dto.getId() == null || !StringUtils.hasText(dto.getName())) {
                skipped++;
                continue;
            }
            Faculty faculty = null;
            if (dto.getParent() != null) {
                faculty = facultyRepository.findByHemisId(dto.getParent()).orElse(null);
            }

            Department department = departmentRepository.findByHemisId(dto.getId()).orElse(null);
            if (department == null) {
                department = Department.builder()
                        .name(dto.getName())
                        .hemisId(dto.getId())
                        .code(dto.getCode())
                        .hemisActive(Boolean.TRUE.equals(dto.getActive()))
                        .structureTypeCode(codeOf(dto))
                        .parentHemisId(dto.getParent())
                        .faculty(faculty)
                        .build();
                department.setCreatedUsername(SecurityUtils.getCurrentUsername());
                departmentRepository.save(department);
                created++;
            } else {
                department.setName(dto.getName());
                department.setCode(dto.getCode());
                department.setHemisActive(Boolean.TRUE.equals(dto.getActive()));
                department.setStructureTypeCode(codeOf(dto));
                department.setParentHemisId(dto.getParent());
                if (faculty != null) {
                    department.setFaculty(faculty);
                }
                departmentRepository.save(department);
                updated++;
            }
        }

        return result(items.size(), created, updated, skipped);
    }

    private HemisDepartmentQuery normalize(HemisDepartmentQuery query, boolean forSync) {
        HemisDepartmentQuery q = query == null ? new HemisDepartmentQuery() : query;
        if (q.getPage() == null || q.getPage() < 1) {
            q.setPage(1);
        }
        if (q.getLimit() == null || q.getLimit() < 1) {
            q.setLimit(50);
        }
        if (!StringUtils.hasText(q.getActive())) {
            q.setActive("1");
        }
        if (forSync) {
            q.setFetchAllPages(true);
        }
        return q;
    }

    private String codeOf(HemisDepartmentDto dto) {
        return dto.getStructureType() != null ? dto.getStructureType().getCode() : null;
    }

    private HemisSyncResult result(int fetched, int created, int updated, int skipped) {
        return HemisSyncResult.builder()
                .fetched(fetched)
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.urspi.allocate.hemis.dto.HemisDepartmentQuery;
import uz.urspi.allocate.hemis.dto.HemisTokenRequest;
import uz.urspi.allocate.hemis.response.HemisDepartmentListResponse;
import uz.urspi.allocate.hemis.response.HemisSyncResult;
import uz.urspi.allocate.hemis.response.HemisTokenResponse;
import uz.urspi.allocate.hemis.service.HemisSyncService;
import uz.urspi.allocate.hemis.service.HemisTokenService;

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
    @PreAuthorize("hasAnyAuthority('EXTERNAL_TOKEN_VIEW','FACULTY_VIEW','DEPARTMENT_VIEW')")
    public ResponseEntity<HemisDepartmentListResponse> departments(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String active,
            @RequestParam(name = "_structure_type", required = false) String structureType,
            @RequestParam(required = false) Long parent
    ) {
        HemisDepartmentQuery query = new HemisDepartmentQuery();
        query.setPage(page);
        query.setLimit(limit);
        query.setActive(active);
        query.setStructureType(structureType);
        query.setParent(parent);
        query.setFetchAllPages(false);
        return ResponseEntity.ok(hemisSyncService.fetchDepartments(query));
    }

    @PostMapping("/sync/faculties")
    @PreAuthorize("hasAnyAuthority('FACULTY_CREATE','FACULTY_EDIT','EXTERNAL_TOKEN_EDIT')")
    public ResponseEntity<HemisSyncResult> syncFaculties(@RequestBody(required = false) HemisDepartmentQuery query) {
        return ResponseEntity.ok(hemisSyncService.syncFaculties(query));
    }

    @PostMapping("/sync/departments")
    @PreAuthorize("hasAnyAuthority('DEPARTMENT_CREATE','DEPARTMENT_EDIT','EXTERNAL_TOKEN_EDIT')")
    public ResponseEntity<HemisSyncResult> syncDepartments(@RequestBody(required = false) HemisDepartmentQuery query) {
        return ResponseEntity.ok(hemisSyncService.syncDepartments(query));
    }
}""",
)

w(
    "department/entity/Department.java",
    """
package uz.urspi.allocate.department.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.urspi.allocate.common.entity.BaseEntity;
import uz.urspi.allocate.faculty.entity.Faculty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "departments")
@SQLRestriction("status <> 'DELETED'")
public class Department extends BaseEntity {

    @EqualsAndHashCode.Include
    private String name;

    @Column(unique = true)
    private Long hemisId;

    private String code;

    private Boolean hemisActive;

    private String structureTypeCode;

    private Long parentHemisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;
}
""",
)

w(
    "department/repository/DepartmentRepository.java",
    """
package uz.urspi.allocate.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.department.entity.Department;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByHemisId(Long hemisId);
}
""",
)

w(
    "department/response/DepartmentResponse.java",
    """
package uz.urspi.allocate.department.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.common.enums.EntityStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private Long id;
    private String name;
    private EntityStatus status;
    private Long facultyId;
    private String facultyName;
    private Long hemisId;
    private String code;
}
""",
)

print("done")
PY
