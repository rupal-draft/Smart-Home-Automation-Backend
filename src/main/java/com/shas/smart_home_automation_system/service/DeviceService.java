package com.shas.smart_home_automation_system.service;

import com.shas.smart_home_automation_system.dto.DeviceDto;
import com.shas.smart_home_automation_system.enums.DeviceStatus;

import java.util.List;

public interface DeviceService {

    List<DeviceDto> getUserDevices(Long userId);

    List<DeviceDto> getHomeDevices(Long homeId, Long userId);

    DeviceDto createDevice(DeviceDto deviceDto, Long userId);

    DeviceDto updateDeviceStatus(Long deviceId, DeviceStatus status, Long userId);

    void deleteDevice(Long deviceId, Long userId);

    Double getHomePowerConsumption(Long homeId, Long userId);
}
