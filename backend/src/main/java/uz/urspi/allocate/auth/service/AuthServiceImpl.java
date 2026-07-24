package uz.urspi.allocate.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.auth.dto.LoginRequest;
import uz.urspi.allocate.auth.dto.RefreshRequest;
import uz.urspi.allocate.auth.response.AuthResponse;
import uz.urspi.allocate.common.enums.AuditAction;
import uz.urspi.allocate.security.JwtService;
import uz.urspi.allocate.security.TokenBlacklistService;
import uz.urspi.allocate.user.entity.User;
import uz.urspi.allocate.user.mapper.UserMapper;
import uz.urspi.allocate.user.repository.UserRepository;
import uz.urspi.allocate.user.response.UserResponse;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    @Auditable(entity = "User", action = AuditAction.LOGIN)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsernameWithRoles(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    @Auditable(entity = "User", action = AuditAction.REFRESH_TOKEN)
    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new BadCredentialsException("Refresh token is invalid or expired"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadCredentialsException("Refresh token is invalid or expired");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return buildAuthResponse(user, newAccessToken, refreshToken);
    }

    @Override
    @Auditable(entity = "User", action = AuditAction.LOGOUT)
    public void logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null && jwtService.isTokenValid(token)) {
            long ttlMs = jwtService.getExpiration(token).toEpochMilli() - Instant.now().toEpochMilli();
            tokenBlacklistService.blacklist(token, ttlMs);
        }
        SecurityContextHolder.clearContext();
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        UserResponse userResponse = UserMapper.toResponse(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .user(userResponse)
                .build();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
