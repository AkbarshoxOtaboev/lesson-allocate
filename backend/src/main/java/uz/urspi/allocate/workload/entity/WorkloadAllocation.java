package uz.urspi.allocate.workload.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.teacher.entity.Teacher;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "workload_allocations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "teacher_id"})
)
@SQLRestriction("status <> 'DELETED'")
public class WorkloadAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Builder.Default
    private Integer lectureHours = 0;

    @Builder.Default
    private Integer seminarHours = 0;

    @Builder.Default
    private Integer practicalHours = 0;

    @Builder.Default
    private Integer labHours = 0;

    @Builder.Default
    private Integer ratingHours = 0;
}
