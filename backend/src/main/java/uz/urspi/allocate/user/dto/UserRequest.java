package uz.urspi.allocate.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String username;
    private String password;
    private String fullName;
    private String phone;
    private List<Long> roleIds;
    private MultipartFile profileImage;
    private Long facultyId;
    private Long departmentId;
}
