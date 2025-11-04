package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.UserDto;
import com.shas.smart_home_automation_system.entity.User;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.UserRepository;
import com.shas.smart_home_automation_system.service.UserService;
import com.shas.smart_home_automation_system.util.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public User getUserFromId(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(()-> new AuthenticationCredentialsNotFoundException("User not found!"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        String cacheKey = "all";

        @SuppressWarnings("unchecked")
        List<UserDto> cachedUsers = cacheService.get("users", cacheKey, List.class);
        if (cachedUsers != null) return cachedUsers;
        log.info("Fetching all users from database...");
        List<UserDto> users = userRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
        cacheService.put("users", cacheKey, users, 10, TimeUnit.MINUTES);
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        UserDto cachedUser = cacheService.get("user", id.toString(), UserDto.class);
        if (cachedUser != null) return cachedUser;
        log.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        UserDto dto = convertToDto(user);
        cacheService.put("user", id.toString(), dto, 10, TimeUnit.MINUTES);
        return dto;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User currentUser = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + currentUser.getId()));

        if (userDto.getFirstName() != null) user.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null) user.setLastName(userDto.getLastName());

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(user);
        UserDto dto = convertToDto(updatedUser);

        cacheService.put("user", String.valueOf(currentUser.getId()), dto, 10, TimeUnit.MINUTES);
        cacheService.evict("users", "all");

        return dto;
    }


    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        cacheService.evict("user", id.toString());
        cacheService.evict("users", "all");
        log.info("Deleted user with id {}", id);
    }

    private UserDto convertToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }
}
