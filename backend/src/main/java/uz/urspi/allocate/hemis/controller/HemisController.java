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
import uz.urspi.allocate.hemis.dto.HemisEmployeeQuery;
import uz.urspi.allocate.hemis.dto.HemisGroupQuery;
import uz.urspi.allocate.hemis.dto.HemisSpecialtyQuery;
import uz.urspi.allocate.hemis.dto.HemisTokenRequest;
import uz.urspi.allocate.hemis.response.HemisDepartmentListResponse;
import uz.urspi.allocate.hemis.response.HemisEmployeeListResponse;
import uz.urspi.allocate.hemis.response.HemisGroupListResponse;
import uz.urspi.allocate.hemis.response.HemisSpecialtyListResponse;
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

    @GetMapping("/employees")
    @PreAuthorize("hasAnyAuthority('EXTERNAL_TOKEN_VIEW','TEACHER_VIEW','EMPLOYEE_VIEW')")
    public ResponseEntity<HemisEmployeeListResponse> employees(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false, defaultValue = "teacher") String type,
            @RequestParam(name = "_department", required = false) Long department,
            @RequestParam(name = "_gender", required = false) String gender,
            @RequestParam(name = "_staff_position", required = false) String staffPosition,
            @RequestParam(name = "_employee_status", required = false) String employeeStatus,
            @RequestParam(name = "_employment_form", required = false) String employmentForm,
            @RequestParam(name = "_employment_staff", required = false) String employmentStaff,
            @RequestParam(name = "_employee_type", required = false) String employeeType,
            @RequestParam(name = "_academic_rank", required = false) String academicRank,
            @RequestParam(name = "_academic_degree", required = false) String academicDegree,
            @RequestParam(name = "passport_pin", required = false) String passportPin,
            @RequestParam(name = "passport_number", required = false) String passportNumber,
            @RequestParam(required = false) String search
    ) {
        HemisEmployeeQuery query = new HemisEmployeeQuery();
        query.setPage(page);
        query.setLimit(limit);
        query.setType(type);
        query.setDepartment(department);
        query.setGender(gender);
        query.setStaffPosition(staffPosition);
        query.setEmployeeStatus(employeeStatus);
        query.setEmploymentForm(employmentForm);
        query.setEmploymentStaff(employmentStaff);
        query.setEmployeeType(employeeType);
        query.setAcademicRank(academicRank);
        query.setAcademicDegree(academicDegree);
        query.setPassportPin(passportPin);
        query.setPassportNumber(passportNumber);
        query.setSearch(search);
        query.setFetchAllPages(false);
        return ResponseEntity.ok(hemisSyncService.fetchEmployees(query));
    }

    @PostMapping("/sync/teachers")
    @PreAuthorize("hasAnyAuthority('TEACHER_CREATE','TEACHER_EDIT','EXTERNAL_TOKEN_EDIT')")
    public ResponseEntity<HemisSyncResult> syncTeachers(@RequestBody(required = false) HemisEmployeeQuery query) {
        return ResponseEntity.ok(hemisSyncService.syncTeachers(query));
    }

    @GetMapping("/groups")
    @PreAuthorize("hasAnyAuthority('EXTERNAL_TOKEN_VIEW','GROUP_VIEW')")
    public ResponseEntity<HemisGroupListResponse> groups(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Long id,
            @RequestParam(name = "_department", required = false) Long department,
            @RequestParam(name = "_curriculum", required = false) Long curriculum,
            @RequestParam(name = "_specialty", required = false) Long specialty,
            @RequestParam(name = "_education_type", required = false) String educationType,
            @RequestParam(name = "_education_form", required = false) String educationForm
    ) {
        HemisGroupQuery query = new HemisGroupQuery();
        query.setPage(page);
        query.setLimit(limit);
        query.setId(id);
        query.setDepartment(department);
        query.setCurriculum(curriculum);
        query.setSpecialty(specialty);
        query.setEducationType(educationType);
        query.setEducationForm(educationForm);
        query.setFetchAllPages(false);
        return ResponseEntity.ok(hemisSyncService.fetchGroups(query));
    }

    @PostMapping("/sync/groups")
    @PreAuthorize("hasAnyAuthority('GROUP_CREATE','GROUP_EDIT','EXTERNAL_TOKEN_EDIT')")
    public ResponseEntity<HemisSyncResult> syncGroups(@RequestBody(required = false) HemisGroupQuery query) {
        return ResponseEntity.ok(hemisSyncService.syncGroups(query));
    }

    @GetMapping("/specialties")
    @PreAuthorize("hasAnyAuthority('EXTERNAL_TOKEN_VIEW','DIRECTION_VIEW')")
    public ResponseEntity<HemisSpecialtyListResponse> specialties(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(name = "_department", required = false) Long department,
            @RequestParam(name = "_locality_type", required = false) String localityType,
            @RequestParam(name = "_education_type", required = false) String educationType
    ) {
        HemisSpecialtyQuery query = new HemisSpecialtyQuery();
        query.setPage(page);
        query.setLimit(limit);
        query.setDepartment(department);
        query.setLocalityType(localityType);
        query.setEducationType(educationType);
        query.setFetchAllPages(false);
        return ResponseEntity.ok(hemisSyncService.fetchSpecialties(query));
    }

    @PostMapping("/sync/directions")
    @PreAuthorize("hasAnyAuthority('DIRECTION_CREATE','DIRECTION_EDIT','EXTERNAL_TOKEN_EDIT')")
    public ResponseEntity<HemisSyncResult> syncDirections(@RequestBody(required = false) HemisSpecialtyQuery query) {
        return ResponseEntity.ok(hemisSyncService.syncDirections(query));
    }
}
