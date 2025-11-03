package com.shas.smart_home_automation_system.service;

import com.shas.smart_home_automation_system.dto.AuthDto;

public interface AuthService {

    AuthDto.JwtResponse authenticateUser(AuthDto.LoginRequest loginRequest);

    AuthDto.JwtResponse registerUser(AuthDto.RegisterRequest registerRequest);
}
