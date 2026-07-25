package uz.urspi.allocate.hemis.service;

import uz.urspi.allocate.hemis.dto.HemisDepartmentQuery;
import uz.urspi.allocate.hemis.dto.HemisEmployeeQuery;
import uz.urspi.allocate.hemis.response.HemisDepartmentListResponse;
import uz.urspi.allocate.hemis.response.HemisEmployeeListResponse;
import uz.urspi.allocate.hemis.response.HemisSyncResult;

public interface HemisSyncService {

    HemisDepartmentListResponse fetchDepartments(HemisDepartmentQuery query);

    HemisSyncResult syncFaculties(HemisDepartmentQuery query);

    HemisSyncResult syncDepartments(HemisDepartmentQuery query);

    HemisEmployeeListResponse fetchEmployees(HemisEmployeeQuery query);

    HemisSyncResult syncTeachers(HemisEmployeeQuery query);
}
