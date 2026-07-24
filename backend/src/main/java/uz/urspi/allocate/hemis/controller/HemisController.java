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
}
