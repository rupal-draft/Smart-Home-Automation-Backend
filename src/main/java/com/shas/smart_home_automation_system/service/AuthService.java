package com.shas.smart_home_automation_system.service;

import com.shas.smart_home_automation_system.dto.JwtResponseDto;
import com.shas.smart_home_automation_system.dto.LoginRequestDto;
import com.shas.smart_home_automation_system.dto.RegisterRequestDto;

public interface AuthService {

    JwtResponseDto authenticateUser(LoginRequestDto loginRequest);

    JwtResponseDto registerUser(RegisterRequestDto registerRequest);

    JwtResponseDto refreshToken(String refreshToken);
}
