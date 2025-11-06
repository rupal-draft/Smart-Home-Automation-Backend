package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.JwtResponseDto;
import com.shas.smart_home_automation_system.dto.LoginRequestDto;
import com.shas.smart_home_automation_system.dto.RegisterRequestDto;
import com.shas.smart_home_automation_system.entity.User;
import com.shas.smart_home_automation_system.repository.UserRepository;
import com.shas.smart_home_automation_system.security.JwtService;
import com.shas.smart_home_automation_system.service.AuthService;
import com.shas.smart_home_automation_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public JwtResponseDto authenticateUser(@Valid LoginRequestDto loginRequest) {
        String username = loginRequest.getUsername();
        log.info("Authenticating user: {}", username);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username, loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            String accessToken = jwtService.generateAccessKey(user);
            String refreshToken = jwtService.generateRefreshJwtToken(user);

            JwtResponseDto jwtResponse = new JwtResponseDto(
                    accessToken,
                    refreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles()
            );

            log.info("User '{}' authenticated successfully", username);

            return jwtResponse;

        } catch (Exception e) {
            log.error("Authentication failed for '{}': {}", username, e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Override
    public JwtResponseDto registerUser(@Valid RegisterRequestDto registerRequest) {
        String username = registerRequest.getUsername();
        log.info("Registering new user: {}", username);

        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: username '{}' is already taken", username);
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed: email '{}' is already in use", registerRequest.getEmail());
            throw new RuntimeException("Email is already in use");
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setEmail(registerRequest.getEmail());
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setRoles(new HashSet<>());
            user.getRoles().add("ROLE_USER");

            userRepository.save(user);
            log.info("User '{}' registered successfully", username);

            LoginRequestDto loginRequest = new LoginRequestDto();
            loginRequest.setUsername(username);
            loginRequest.setPassword(registerRequest.getPassword());

            return authenticateUser(loginRequest);

        } catch (Exception e) {
            log.error("Registration failed for '{}': {}", username, e.getMessage());
            throw new RuntimeException("User registration failed");
        }
    }

    @Override
    public JwtResponseDto refreshToken(String refreshToken) {
        Long userId = jwtService.getUserId(refreshToken);
        User user = userService.getUserFromId(userId);
        if(user == null) throw new AuthenticationCredentialsNotFoundException("User not found");

        String accessToken = jwtService.generateAccessKey(user);

        JwtResponseDto jwtResponse = new JwtResponseDto(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );

        log.info("Refresh token generated successfully for userId: {}", userId);

        return jwtResponse;
    }
}
