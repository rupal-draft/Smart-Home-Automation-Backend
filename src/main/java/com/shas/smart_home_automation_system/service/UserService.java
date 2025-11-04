package com.shas.smart_home_automation_system.service;

import com.shas.smart_home_automation_system.dto.UserDto;
import com.shas.smart_home_automation_system.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    User getUserFromId(Long userId);

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto updateUser(UserDto userDto);

    void deleteUser(Long id);
}
