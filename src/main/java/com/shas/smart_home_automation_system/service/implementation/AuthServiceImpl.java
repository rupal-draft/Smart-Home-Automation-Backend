package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.AuthDto;
import com.shas.smart_home_automation_system.entity.User;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.UserRepository;
import com.shas.smart_home_automation_system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "authTokens", key = "#loginRequest.username")
    public AuthDto.JwtResponse authenticateUser(@Valid AuthDto.LoginRequest loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with username: " + loginRequest.getUsername()
                    ));

            log.info("User '{}' authenticated successfully", loginRequest.getUsername());
            return new AuthDto.JwtResponse(jwt, user.getId(), user.getUsername(),
                    user.getEmail(), user.getRoles());

        } catch (Exception e) {
            log.error("Authentication failed for '{}': {}", loginRequest.getUsername(), e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Override
    @CacheEvict(value = "authTokens", key = "#registerRequest.username")
    public AuthDto.JwtResponse registerUser(@Valid AuthDto.RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getUsername());

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Registration failed: username '{}' is already taken", registerRequest.getUsername());
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed: email '{}' is already in use", registerRequest.getEmail());
            throw new RuntimeException("Email is already in use");
        }

        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setEmail(registerRequest.getEmail());
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setRoles(new HashSet<>());
            user.getRoles().add("ROLE_USER");

            userRepository.save(user);
            log.info("User '{}' registered successfully", registerRequest.getUsername());

            AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
            loginRequest.setUsername(registerRequest.getUsername());
            loginRequest.setPassword(registerRequest.getPassword());

            return authenticateUser(loginRequest);

        } catch (Exception e) {
            log.error("Registration failed for '{}': {}", registerRequest.getUsername(), e.getMessage());
            throw new RuntimeException("User registration failed");
        }
    }
}