package uz.urspi.allocate.subject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectGroupResponse {

    private Long id;
    private String name;
    private Integer studentCount;
}
