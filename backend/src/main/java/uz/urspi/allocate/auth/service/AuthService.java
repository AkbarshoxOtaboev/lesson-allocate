package uz.urspi.allocate.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import uz.urspi.allocate.auth.dto.LoginRequest;
import uz.urspi.allocate.auth.dto.RefreshRequest;
import uz.urspi.allocate.auth.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(HttpServletRequest request);
}
