package uz.urspi.allocate.workload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.workload.enums.AllocationStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadRowResponse {

    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private Long departmentId;
    private String departmentName;
    private Long facultyId;
    private String facultyName;
    /** Talabnoma orqali kelgan fan uchun yuborgan fakultet */
    private Long sourceFacultyId;
    private String sourceFacultyName;
    private String talabnomaCode;
    private Semester semester;
    private Integer courseYear;
    private Integer lectureHours;
    private Integer seminarHours;
    private Integer practicalHours;
    private Integer labHours;
    private Integer ratingHours;
    private Integer independentStudyHours;
    /** Auditorik soat (ma'ruza + seminar + amaliy + lab + reyting) */
    private Integer totalHours;
    private Integer allocatedHours;
    private Integer remainingHours;
    private Integer groupCount;
    private Integer studentCount;
    private AllocationStatus allocationStatus;
}
