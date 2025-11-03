package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.AuthDto;
import com.shas.smart_home_automation_system.entity.User;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.UserRepository;
import com.shas.smart_home_automation_system.service.AuthService;
import com.shas.smart_home_automation_system.util.CacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CacheService cacheService;

    private static final String AUTH_CACHE = "authTokens";

    @Override
    @Transactional(readOnly = true)
    public AuthDto.JwtResponse authenticateUser(@Valid AuthDto.LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        log.info("Authenticating user: {}", username);

        AuthDto.JwtResponse cachedResponse = cacheService.get(AUTH_CACHE, username, AuthDto.JwtResponse.class);
        if (cachedResponse != null) {
            log.info("Returning cached JWT token for user: {}", username);
            return cachedResponse;
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username, loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with username: " + username
                    ));

            AuthDto.JwtResponse jwtResponse = new AuthDto.JwtResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles()
            );

            log.info("User '{}' authenticated successfully", username);

            cacheService.put(AUTH_CACHE, username, jwtResponse, 1, TimeUnit.HOURS);

            return jwtResponse;

        } catch (Exception e) {
            log.error("Authentication failed for '{}': {}", username, e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Override
    public AuthDto.JwtResponse registerUser(@Valid AuthDto.RegisterRequest registerRequest) {
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

            cacheService.evict(AUTH_CACHE, username);

            AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
            loginRequest.setUsername(username);
            loginRequest.setPassword(registerRequest.getPassword());

            return authenticateUser(loginRequest);

        } catch (Exception e) {
            log.error("Registration failed for '{}': {}", username, e.getMessage());
            throw new RuntimeException("User registration failed");
        }
    }
}
