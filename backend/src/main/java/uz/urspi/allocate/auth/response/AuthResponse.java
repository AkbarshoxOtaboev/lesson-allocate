package uz.urspi.allocate.auth.response;

import lombok.Builder;
import lombok.Getter;
import uz.urspi.allocate.user.response.UserResponse;

@Getter
@Builder
public class AuthResponse {

    private final String accessToken;
    private final String refreshToken;

    @Builder.Default
    private final String tokenType = "Bearer";

    private final String username;
    private final String fullName;
    private final UserResponse user;
}
