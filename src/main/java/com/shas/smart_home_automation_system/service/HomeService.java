package com.shas.smart_home_automation_system.service;

import com.shas.smart_home_automation_system.dto.HomeDto;

import java.util.List;

public interface HomeService {

    List<HomeDto> getUserHomes(Long userId);

    HomeDto getHomeById(Long homeId, Long userId);

    HomeDto createHome(HomeDto homeDto, Long userId);

    HomeDto updateHome(Long homeId, HomeDto homeDto, Long userId);

    void deleteHome(Long homeId, Long userId);
}
