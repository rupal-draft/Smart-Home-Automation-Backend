package com.shas.smart_home_automation_system.controller;

import com.shas.smart_home_automation_system.dto.JwtResponseDto;
import com.shas.smart_home_automation_system.dto.LoginRequestDto;
import com.shas.smart_home_automation_system.dto.RegisterRequestDto;
import com.shas.smart_home_automation_system.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            HttpServletResponse response
    ) {
        JwtResponseDto jwtResponse = authService.authenticateUser(loginRequest);
        addRefreshTokenCookie(response, jwtResponse.getRefreshToken());
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponseDto> register(
            @Valid @RequestBody RegisterRequestDto registerRequest,
            HttpServletResponse response
    ) {
        JwtResponseDto jwtResponse = authService.registerUser(registerRequest);
        addRefreshTokenCookie(response, jwtResponse.getRefreshToken());
        return ResponseEntity.ok(jwtResponse);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure("prod".equalsIgnoreCase(activeProfile));
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }
}

