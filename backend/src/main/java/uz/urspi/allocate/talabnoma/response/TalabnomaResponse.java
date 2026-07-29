package uz.urspi.allocate.talabnoma.response;

import lombok.Builder;
import lombok.Getter;
import uz.urspi.allocate.subject.enums.EducationLanguage;
import uz.urspi.allocate.subject.enums.EducationType;
import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.talabnoma.enums.TalabnomaStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TalabnomaResponse {
    private Long id;
    private String code;
    private Long fromFacultyId;
    private String fromFacultyName;
    private Long toDepartmentId;
    private String toDepartmentName;
    private Long toFacultyId;
    private String toFacultyName;
    private String subjectName;
    private String subjectCode;
    private Long academicYearId;
    private String academicYearName;
    private Long directionId;
    private String directionCode;
    private String directionName;
    private Semester semester;
    private EducationType educationType;
    private EducationLanguage educationLanguage;
    private Integer totalSubjectHours;
    private Integer lectureHours;
    private Integer practicalHours;
    private Integer labHours;
    private Integer seminarHours;
    private Integer independentStudyHours;
    private Integer ratingHours;
    private Integer totalHours;
    private Integer groupCount;
    private Integer studentCount;
    private Integer allocatedHours;
    private TalabnomaStatus requestStatus;
    private String note;
    private String rejectReason;
    private Long linkedSubjectId;
    private String createdByName;
    private LocalDateTime createdAt;
    private List<AllocatedTeacherInfo> allocatedTeachers;

    @Getter
    @Builder
    public static class AllocatedTeacherInfo {
        private Long teacherId;
        private String teacherName;
        private Integer hours;
    }
}
