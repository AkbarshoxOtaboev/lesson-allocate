package uz.urspi.allocate.workload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.workload.enums.AllocationStatus;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadDetailResponse {

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
    private Double credit;
    private Integer totalSubjectHours;
    private Integer independentStudyHours;
    private Integer totalHours;
    private Integer allocatedHours;
    private Integer remainingHours;
    private AllocationStatus allocationStatus;

    private HourBucket lecture;
    private HourBucket seminar;
    private HourBucket practical;
    private HourBucket lab;
    private HourBucket rating;

    private List<AllocationItem> allocations;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourBucket {
        private Integer total;
        private Integer allocated;
        private Integer remaining;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationItem {
        private Long id;
        private Long teacherId;
        private String teacherName;
        private Integer lectureHours;
        private Integer seminarHours;
        private Integer practicalHours;
        private Integer labHours;
        private Integer ratingHours;
        private Integer totalHours;
    }
}
