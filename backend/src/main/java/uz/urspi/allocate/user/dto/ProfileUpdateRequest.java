package uz.urspi.allocate.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    private String fullName;
    private String phone;
    private String bio;
    private String country;
    private String city;
    private String region;
    private String postalCode;
    private String taxId;
}
