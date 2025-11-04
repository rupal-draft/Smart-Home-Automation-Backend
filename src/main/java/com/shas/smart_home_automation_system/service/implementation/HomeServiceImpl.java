package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.HomeDto;
import com.shas.smart_home_automation_system.entity.Home;
import com.shas.smart_home_automation_system.entity.User;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.HomeRepository;
import com.shas.smart_home_automation_system.repository.UserRepository;
import com.shas.smart_home_automation_system.service.HomeService;
import com.shas.smart_home_automation_system.util.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomeRepository homeRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final ModelMapper modelMapper;

    private static final String CACHE_NAME = "homes";
    private static final long CACHE_TTL = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomeDto> getUserHomes() {
        User user = getAuthenticatedUser();
        Long userId = user.getId();
        String cacheKey = "userHomes:" + userId;

        @SuppressWarnings("unchecked")
        List<HomeDto> cachedHomes = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedHomes != null) {
            log.info("Returning homes from cache for userId: {}", userId);
            return cachedHomes;
        }

        log.info("Fetching homes from DB for userId: {}", userId);
        List<HomeDto> homes = homeRepository.findByUser(user)
                .stream()
                .map(this::convertToDto)
                .toList();

        cacheService.put(CACHE_NAME, cacheKey, homes, CACHE_TTL, TIME_UNIT);
        return homes;
    }

    @Override
    @Transactional(readOnly = true)
    public HomeDto getHomeById(Long homeId) {
        User user = getAuthenticatedUser();
        Long userId = user.getId();
        String cacheKey = "home:" + homeId + ":user:" + userId;

        HomeDto cachedHome = cacheService.get(CACHE_NAME, cacheKey, HomeDto.class);
        if (cachedHome != null) {
            log.info("Returning home {} for user {} from cache", homeId, userId);
            return cachedHome;
        }

        log.info("Fetching home {} for user {} from DB", homeId, userId);
        Home home = homeRepository.findByIdAndUser(homeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found with id: " + homeId));

        HomeDto homeDto = convertToDto(home);
        cacheService.put(CACHE_NAME, cacheKey, homeDto, CACHE_TTL, TIME_UNIT);

        return homeDto;
    }

    @Override
    public HomeDto createHome(HomeDto homeDto) {
        User user = getAuthenticatedUser();
        Long userId = user.getId();

        log.info("Creating new home for userId: {}", userId);
        Home home = new Home();
        home.setName(homeDto.getName());
        home.setAddress(homeDto.getAddress());
        home.setTimezone(homeDto.getTimezone());
        home.setUser(user);

        Home savedHome = homeRepository.save(home);
        HomeDto savedDto = convertToDto(savedHome);

        cacheService.evict(CACHE_NAME, "userHomes:" + userId);
        return savedDto;
    }

    @Override
    public HomeDto updateHome(Long homeId, HomeDto homeDto) {
        User user = getAuthenticatedUser();
        Long userId = user.getId();

        log.info("Updating home {} for user {}", homeId, userId);
        Home home = homeRepository.findByIdAndUser(homeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found with id: " + homeId));

        home.setName(homeDto.getName());
        home.setAddress(homeDto.getAddress());
        home.setTimezone(homeDto.getTimezone());

        Home updatedHome = homeRepository.save(home);
        HomeDto updatedDto = convertToDto(updatedHome);

        cacheService.put(CACHE_NAME, "home:" + homeId + ":user:" + userId, updatedDto, CACHE_TTL, TIME_UNIT);
        cacheService.evict(CACHE_NAME, "userHomes:" + userId);

        return updatedDto;
    }

    @Override
    public void deleteHome(Long homeId) {
        User user = getAuthenticatedUser();
        Long userId = user.getId();

        log.info("Deleting home {} for user {}", homeId, userId);
        Home home = homeRepository.findByIdAndUser(homeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found with id: " + homeId));

        homeRepository.delete(home);

        cacheService.evict(CACHE_NAME, "home:" + homeId + ":user:" + userId);
        cacheService.evict(CACHE_NAME, "userHomes:" + userId);
    }

    private HomeDto convertToDto(Home home) {
        return modelMapper.map(home, HomeDto.class);
    }
}
