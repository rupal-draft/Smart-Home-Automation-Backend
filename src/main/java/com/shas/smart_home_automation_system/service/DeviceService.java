package com.shas.smart_home_automation_system.service;

import com.shas.smart_home_automation_system.dto.DeviceDto;
import com.shas.smart_home_automation_system.enums.DeviceStatus;

import java.util.List;

public interface DeviceService {

    List<DeviceDto> getUserDevices();

    List<DeviceDto> getHomeDevices(Long homeId);

    DeviceDto createDevice(DeviceDto deviceDto);

    DeviceDto updateDeviceStatus(Long deviceId, DeviceStatus status);

    void deleteDevice(Long deviceId);

    Double getHomePowerConsumption(Long homeId);
}
