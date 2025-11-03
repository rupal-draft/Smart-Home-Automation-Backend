package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.UserDto;
import com.shas.smart_home_automation_system.entity.User;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.UserRepository;
import com.shas.smart_home_automation_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users")
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users from database...");
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user", key = "#id")
    public UserDto getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDto(user);
    }


    @Override
    @CachePut(value = "user", key = "#id")
    @CacheEvict(value = "users", allEntries = true)
    public UserDto updateUser(Long id, UserDto userDto) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setEmail(userDto.getEmail());

            User updatedUser = userRepository.save(user);
            log.info("Updated user: {}", updatedUser.getUsername());
            return convertToDto(updatedUser);
        } catch (Exception ex) {
            log.error("Failed to update user with id {}: {}", id, ex.getMessage());
            throw ex;
        }
    }


    @Override
    @CacheEvict(value = { "user", "users" }, key = "#id", allEntries = true)
    public void deleteUser(Long id) {
        try {
            if (!userRepository.existsById(id)) {
                throw new ResourceNotFoundException("User not found with id: " + id);
            }
            userRepository.deleteById(id);
            log.info("Deleted user with id {}", id);
        } catch (Exception ex) {
            log.error("Failed to delete user with id {}: {}", id, ex.getMessage());
            throw ex;
        }
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRoles(user.getRoles());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
