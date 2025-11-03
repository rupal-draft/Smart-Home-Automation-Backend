package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.HomeDto;
import com.shas.smart_home_automation_system.entity.Home;
import com.shas.smart_home_automation_system.entity.User;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.HomeRepository;
import com.shas.smart_home_automation_system.repository.UserRepository;
import com.shas.smart_home_automation_system.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "homes")
public class HomeServiceImpl implements HomeService {

    private final HomeRepository homeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'userHomes:' + #userId")
    public List<HomeDto> getUserHomes(Long userId) {
        log.info("Fetching homes from DB for userId: {}", userId);
        return homeRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'home:' + #homeId + ':user:' + #userId")
    public HomeDto getHomeById(Long homeId, Long userId) {
        log.info("Fetching home {} for user {}", homeId, userId);
        Home home = homeRepository.findByIdAndUserId(homeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found with id: " + homeId));
        return convertToDto(home);
    }

    @Override
    @CacheEvict(key = "'userHomes:' + #userId", allEntries = true)
    public HomeDto createHome(HomeDto homeDto, Long userId) {
        log.info("Creating new home for userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Home home = new Home();
        home.setName(homeDto.getName());
        home.setAddress(homeDto.getAddress());
        home.setTimezone(homeDto.getTimezone());
        home.setUser(user);

        Home savedHome = homeRepository.save(home);
        return convertToDto(savedHome);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'home:' + #homeId + ':user:' + #userId"),
            @CacheEvict(key = "'userHomes:' + #userId")
    })
    public HomeDto updateHome(Long homeId, HomeDto homeDto, Long userId) {
        log.info("Updating home {} for user {}", homeId, userId);
        Home home = homeRepository.findByIdAndUserId(homeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found with id: " + homeId));

        home.setName(homeDto.getName());
        home.setAddress(homeDto.getAddress());
        home.setTimezone(homeDto.getTimezone());

        return convertToDto(homeRepository.save(home));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'home:' + #homeId + ':user:' + #userId"),
            @CacheEvict(key = "'userHomes:' + #userId")
    })
    public void deleteHome(Long homeId, Long userId) {
        log.info("Deleting home {} for user {}", homeId, userId);
        Home home = homeRepository.findByIdAndUserId(homeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found with id: " + homeId));
        homeRepository.delete(home);
    }

    private HomeDto convertToDto(Home home) {
        return HomeDto.builder()
                .id(home.getId())
                .name(home.getName())
                .address(home.getAddress())
                .timezone(home.getTimezone())
                .userId(home.getUser().getId())
                .createdAt(home.getCreatedAt())
                .build();
    }
}
