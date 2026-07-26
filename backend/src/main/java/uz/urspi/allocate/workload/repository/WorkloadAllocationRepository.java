package uz.urspi.allocate.workload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.urspi.allocate.workload.entity.WorkloadAllocation;

import java.util.List;
import java.util.Optional;

public interface WorkloadAllocationRepository extends JpaRepository<WorkloadAllocation, Long> {

    List<WorkloadAllocation> findBySubject_Id(Long subjectId);

    List<WorkloadAllocation> findByTeacher_Id(Long teacherId);

    Optional<WorkloadAllocation> findBySubject_IdAndTeacher_Id(Long subjectId, Long teacherId);

    List<WorkloadAllocation> findBySubject_Department_Id(Long departmentId);

    List<WorkloadAllocation> findBySubject_Department_Faculty_Id(Long facultyId);

    @Query("""
            select coalesce(sum(
                coalesce(a.lectureHours, 0) + coalesce(a.seminarHours, 0)
                + coalesce(a.practicalHours, 0) + coalesce(a.labHours, 0)
                + coalesce(a.ratingHours, 0)
            ), 0)
            from WorkloadAllocation a
            where a.teacher.id = :teacherId
            """)
    long sumHoursByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
            select coalesce(sum(
                coalesce(a.lectureHours, 0) + coalesce(a.seminarHours, 0)
                + coalesce(a.practicalHours, 0) + coalesce(a.labHours, 0)
                + coalesce(a.ratingHours, 0)
            ), 0)
            from WorkloadAllocation a
            where a.subject.id = :subjectId
            """)
    long sumHoursBySubjectId(@Param("subjectId") Long subjectId);
}
