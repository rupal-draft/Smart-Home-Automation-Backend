package com.shas.smart_home_automation_system.controller;

import com.shas.smart_home_automation_system.dto.AuthDto;
import com.shas.smart_home_automation_system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/login")
    public ResponseEntity<AuthDto.JwtResponse> login(@Valid @RequestBody AuthDto.LoginRequest loginRequest) {
        AuthDto.JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDto.JwtResponse> register(@Valid @RequestBody AuthDto.RegisterRequest registerRequest) {
        AuthDto.JwtResponse jwtResponse = authService.registerUser(registerRequest);
        return ResponseEntity.ok(jwtResponse);
    }
}
