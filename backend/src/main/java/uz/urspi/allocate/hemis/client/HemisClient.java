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
import uz.urspi.allocate.hemis.dto.HemisEmployeeDto;
import uz.urspi.allocate.hemis.dto.HemisEmployeeQuery;
import uz.urspi.allocate.hemis.dto.HemisGroupDto;
import uz.urspi.allocate.hemis.dto.HemisGroupQuery;
import uz.urspi.allocate.hemis.dto.HemisSpecialtyDto;
import uz.urspi.allocate.hemis.dto.HemisSpecialtyQuery;
import uz.urspi.allocate.hemis.entity.ExternalToken;
import uz.urspi.allocate.hemis.repository.ExternalTokenRepository;
import uz.urspi.allocate.hemis.response.HemisDepartmentListResponse;
import uz.urspi.allocate.hemis.response.HemisEmployeeListResponse;
import uz.urspi.allocate.hemis.response.HemisGroupListResponse;
import uz.urspi.allocate.hemis.response.HemisSpecialtyListResponse;

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

    public HemisEmployeeListResponse fetchEmployees(HemisEmployeeQuery query) {
        ExternalToken token = requireToken();
        String baseUrl = resolveBaseUrl(token);

        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int limit = query.getLimit() == null ? hemisProperties.getPageSize() : query.getLimit();
        limit = Math.max(1, Math.min(limit, 200));

        if (!query.isFetchAllPages()) {
            return fetchEmployeePage(token, baseUrl, page, limit, query);
        }

        List<HemisEmployeeDto> all = new ArrayList<>();
        int current = 1;
        int pageCount = 1;
        Integer totalCount = null;
        do {
            HemisEmployeeListResponse resp = fetchEmployeePage(token, baseUrl, current, limit, query);
            if (resp.getItems() != null) {
                all.addAll(resp.getItems());
            }
            pageCount = resp.getPageCount() == null ? 1 : resp.getPageCount();
            totalCount = resp.getTotalCount();
            current++;
        } while (current <= pageCount);

        return HemisEmployeeListResponse.builder()
                .items(all)
                .page(1)
                .pageCount(pageCount)
                .pageSize(limit)
                .totalCount(totalCount != null ? totalCount : all.size())
                .build();
    }

    public HemisGroupListResponse fetchGroups(HemisGroupQuery query) {
        ExternalToken token = requireToken();
        String baseUrl = resolveBaseUrl(token);

        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int limit = query.getLimit() == null ? hemisProperties.getPageSize() : query.getLimit();
        limit = Math.max(1, Math.min(limit, 200));

        if (!query.isFetchAllPages()) {
            return fetchGroupPage(token, baseUrl, page, limit, query);
        }

        List<HemisGroupDto> all = new ArrayList<>();
        int current = 1;
        int pageCount = 1;
        Integer totalCount = null;
        do {
            HemisGroupListResponse resp = fetchGroupPage(token, baseUrl, current, limit, query);
            if (resp.getItems() != null) {
                all.addAll(resp.getItems());
            }
            pageCount = resp.getPageCount() == null ? 1 : resp.getPageCount();
            totalCount = resp.getTotalCount();
            current++;
        } while (current <= pageCount);

        return HemisGroupListResponse.builder()
                .items(all)
                .page(1)
                .pageCount(pageCount)
                .pageSize(limit)
                .totalCount(totalCount != null ? totalCount : all.size())
                .build();
    }

    public HemisSpecialtyListResponse fetchSpecialties(HemisSpecialtyQuery query) {
        ExternalToken token = requireToken();
        String baseUrl = resolveBaseUrl(token);

        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int limit = query.getLimit() == null ? hemisProperties.getPageSize() : query.getLimit();
        limit = Math.max(1, Math.min(limit, 200));

        if (!query.isFetchAllPages()) {
            return fetchSpecialtyPage(token, baseUrl, page, limit, query);
        }

        List<HemisSpecialtyDto> all = new ArrayList<>();
        int current = 1;
        int pageCount = 1;
        Integer totalCount = null;
        do {
            HemisSpecialtyListResponse resp = fetchSpecialtyPage(token, baseUrl, current, limit, query);
            if (resp.getItems() != null) {
                all.addAll(resp.getItems());
            }
            pageCount = resp.getPageCount() == null ? 1 : resp.getPageCount();
            totalCount = resp.getTotalCount();
            current++;
        } while (current <= pageCount);

        return HemisSpecialtyListResponse.builder()
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

    private HemisEmployeeListResponse fetchEmployeePage(
            ExternalToken token,
            String baseUrl,
            int page,
            int limit,
            HemisEmployeeQuery query
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(baseUrl + "/v1/data/employee-list")
                .queryParam("page", page)
                .queryParam("limit", limit)
                .queryParam("l", hemisProperties.getLanguage());

        if (StringUtils.hasText(query.getType())) {
            builder.queryParam("type", query.getType());
        }
        if (query.getDepartment() != null) {
            builder.queryParam("_department", query.getDepartment());
        }
        if (StringUtils.hasText(query.getGender())) {
            builder.queryParam("_gender", query.getGender());
        }
        if (StringUtils.hasText(query.getStaffPosition())) {
            builder.queryParam("_staff_position", query.getStaffPosition());
        }
        if (StringUtils.hasText(query.getEmployeeStatus())) {
            builder.queryParam("_employee_status", query.getEmployeeStatus());
        }
        if (StringUtils.hasText(query.getEmploymentForm())) {
            builder.queryParam("_employment_form", query.getEmploymentForm());
        }
        if (StringUtils.hasText(query.getEmploymentStaff())) {
            builder.queryParam("_employment_staff", query.getEmploymentStaff());
        }
        if (StringUtils.hasText(query.getEmployeeType())) {
            builder.queryParam("_employee_type", query.getEmployeeType());
        }
        if (StringUtils.hasText(query.getAcademicRank())) {
            builder.queryParam("_academic_rank", query.getAcademicRank());
        }
        if (StringUtils.hasText(query.getAcademicDegree())) {
            builder.queryParam("_academic_degree", query.getAcademicDegree());
        }
        if (StringUtils.hasText(query.getPassportPin())) {
            builder.queryParam("passport_pin", query.getPassportPin());
        }
        if (StringUtils.hasText(query.getPassportNumber())) {
            builder.queryParam("passport_number", query.getPassportNumber());
        }
        if (StringUtils.hasText(query.getSearch())) {
            builder.queryParam("search", query.getSearch());
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

            List<HemisEmployeeDto> pageItems = employeeList(itemsNode);
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

            return HemisEmployeeListResponse.builder()
                    .items(pageItems)
                    .page(currentPage)
                    .pageCount(pageCount)
                    .pageSize(pageSize)
                    .totalCount(totalCount)
                    .build();
        } catch (RestClientResponseException ex) {
            log.error("HEMIS employee-list failed: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ApiException("HEMIS so'rovi muvaffaqiyatsiz: " + ex.getStatusCode().value(),
                    HttpStatus.BAD_GATEWAY);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("HEMIS employee parse error", ex);
            throw new ApiException("HEMIS javobini o'qib bo'lmadi: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        }
    }

    private HemisGroupListResponse fetchGroupPage(
            ExternalToken token,
            String baseUrl,
            int page,
            int limit,
            HemisGroupQuery query
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(baseUrl + "/v1/data/group-list")
                .queryParam("page", page)
                .queryParam("limit", limit)
                .queryParam("l", hemisProperties.getLanguage());

        if (query.getId() != null) {
            builder.queryParam("id", query.getId());
        }
        if (query.getDepartment() != null) {
            builder.queryParam("_department", query.getDepartment());
        }
        if (query.getCurriculum() != null) {
            builder.queryParam("_curriculum", query.getCurriculum());
        }
        if (query.getSpecialty() != null) {
            builder.queryParam("_specialty", query.getSpecialty());
        }
        if (StringUtils.hasText(query.getEducationType())) {
            builder.queryParam("_education_type", query.getEducationType());
        }
        if (StringUtils.hasText(query.getEducationForm())) {
            builder.queryParam("_education_form", query.getEducationForm());
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

            List<HemisGroupDto> pageItems = groupList(itemsNode);
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

            return HemisGroupListResponse.builder()
                    .items(pageItems)
                    .page(currentPage)
                    .pageCount(pageCount)
                    .pageSize(pageSize)
                    .totalCount(totalCount)
                    .build();
        } catch (RestClientResponseException ex) {
            log.error("HEMIS group-list failed: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ApiException("HEMIS so'rovi muvaffaqiyatsiz: " + ex.getStatusCode().value(),
                    HttpStatus.BAD_GATEWAY);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("HEMIS group parse error", ex);
            throw new ApiException("HEMIS javobini o'qib bo'lmadi: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        }
    }

    private HemisSpecialtyListResponse fetchSpecialtyPage(
            ExternalToken token,
            String baseUrl,
            int page,
            int limit,
            HemisSpecialtyQuery query
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(baseUrl + "/v1/data/specialty-list")
                .queryParam("page", page)
                .queryParam("limit", limit)
                .queryParam("l", hemisProperties.getLanguage());

        if (query.getDepartment() != null) {
            builder.queryParam("_department", query.getDepartment());
        }
        if (StringUtils.hasText(query.getLocalityType())) {
            builder.queryParam("_locality_type", query.getLocalityType());
        }
        if (StringUtils.hasText(query.getEducationType())) {
            builder.queryParam("_education_type", query.getEducationType());
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

            List<HemisSpecialtyDto> pageItems = specialtyList(itemsNode);
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

            return HemisSpecialtyListResponse.builder()
                    .items(pageItems)
                    .page(currentPage)
                    .pageCount(pageCount)
                    .pageSize(pageSize)
                    .totalCount(totalCount)
                    .build();
        } catch (RestClientResponseException ex) {
            log.error("HEMIS specialty-list failed: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ApiException("HEMIS so'rovi muvaffaqiyatsiz: " + ex.getStatusCode().value(),
                    HttpStatus.BAD_GATEWAY);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("HEMIS specialty parse error", ex);
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

    private List<HemisEmployeeDto> employeeList(JsonNode itemsNode) {
        if (itemsNode == null || itemsNode.isNull() || itemsNode.isMissingNode()) {
            return List.of();
        }
        List<HemisEmployeeDto> list = jsonMapper.convertValue(
                itemsNode,
                new TypeReference<List<HemisEmployeeDto>>() {}
        );
        return list == null ? List.of() : list;
    }

    private List<HemisGroupDto> groupList(JsonNode itemsNode) {
        if (itemsNode == null || itemsNode.isNull() || itemsNode.isMissingNode()) {
            return List.of();
        }
        List<HemisGroupDto> list = jsonMapper.convertValue(
                itemsNode,
                new TypeReference<List<HemisGroupDto>>() {}
        );
        return list == null ? List.of() : list;
    }

    private List<HemisSpecialtyDto> specialtyList(JsonNode itemsNode) {
        if (itemsNode == null || itemsNode.isNull() || itemsNode.isMissingNode()) {
            return List.of();
        }
        List<HemisSpecialtyDto> list = jsonMapper.convertValue(
                itemsNode,
                new TypeReference<List<HemisSpecialtyDto>>() {}
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
