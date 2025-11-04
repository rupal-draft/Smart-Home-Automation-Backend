package com.shas.smart_home_automation_system.service;

import com.shas.smart_home_automation_system.dto.HomeDto;

import java.util.List;

public interface HomeService {

    List<HomeDto> getUserHomes();

    HomeDto getHomeById(Long homeId);

    HomeDto createHome(HomeDto homeDto);

    HomeDto updateHome(Long homeId, HomeDto homeDto);

    void deleteHome(Long homeId);
}
