package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.DeviceDto;
import com.shas.smart_home_automation_system.entity.Device;
import com.shas.smart_home_automation_system.entity.Home;
import com.shas.smart_home_automation_system.enums.DeviceStatus;
import com.shas.smart_home_automation_system.enums.DeviceType;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.DeviceRepository;
import com.shas.smart_home_automation_system.repository.HomeRepository;
import com.shas.smart_home_automation_system.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final HomeRepository homeRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "devices_user", key = "#userId")
    public List<DeviceDto> getUserDevices(Long userId) {
        log.info("Fetching all devices for userId: {}", userId);
        return deviceRepository.findByHomeUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "devices_home", key = "#homeId")
    public List<DeviceDto> getHomeDevices(Long homeId, Long userId) {
        log.info("Fetching all devices for homeId: {} and userId: {}", homeId, userId);
        return deviceRepository.findByHomeIdAndHomeUserId(homeId, userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @CacheEvict(value = { "devices_user", "devices_home" }, allEntries = true)
    public DeviceDto createDevice(DeviceDto deviceDto, Long userId) {
        try {
            Home home = homeRepository.findByIdAndUserId(deviceDto.getHomeId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Home not found or access denied"));

            Device device = new Device();
            device.setName(deviceDto.getName());
            device.setDeviceId(deviceDto.getDeviceId());
            device.setType(DeviceType.valueOf(deviceDto.getType()));
            device.setStatus(DeviceStatus.OFFLINE);
            device.setHome(home);
            device.setManufacturer(deviceDto.getManufacturer());
            device.setModel(deviceDto.getModel());
            device.setPowerConsumption(deviceDto.getPowerConsumption());

            Device savedDevice = deviceRepository.save(device);
            log.info("Device created successfully with ID: {}", savedDevice.getId());
            return convertToDto(savedDevice);
        } catch (Exception e) {
            log.error("Error creating device: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @CachePut(value = "device", key = "#deviceId")
    @CacheEvict(value = { "devices_user", "devices_home" }, allEntries = true)
    public DeviceDto updateDeviceStatus(Long deviceId, DeviceStatus status, Long userId) {
        try {
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

            if (!device.getHome().getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Device not found or access denied");
            }

            device.setStatus(status);
            Device updatedDevice = deviceRepository.save(device);
            log.info("Updated status for device {} to {}", deviceId, status);
            return convertToDto(updatedDevice);
        } catch (Exception e) {
            log.error("Failed to update device {}: {}", deviceId, e.getMessage());
            throw e;
        }
    }

    @Override
    @CacheEvict(value = { "device", "devices_user", "devices_home" }, allEntries = true)
    public void deleteDevice(Long deviceId, Long userId) {
        try {
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

            if (!device.getHome().getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Device not found or access denied");
            }

            deviceRepository.delete(device);
            log.info("Deleted device with ID: {}", deviceId);
        } catch (Exception e) {
            log.error("Failed to delete device {}: {}", deviceId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "power_consumption", key = "#homeId")
    public Double getHomePowerConsumption(Long homeId, Long userId) {
        log.info("Fetching power consumption for homeId: {}", homeId);
        homeRepository.findByIdAndUserId(homeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found or access denied"));

        return deviceRepository.getTotalPowerConsumptionByHome(homeId).orElse(0.0);
    }

    private DeviceDto convertToDto(Device device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setDeviceId(device.getDeviceId());
        dto.setType(device.getType().name());
        dto.setStatus(device.getStatus().name());
        dto.setHomeId(device.getHome().getId());
        dto.setRoomId(device.getRoom() != null ? device.getRoom().getId() : null);
        dto.setManufacturer(device.getManufacturer());
        dto.setModel(device.getModel());
        dto.setPowerConsumption(device.getPowerConsumption());
        dto.setCreatedAt(device.getCreatedAt());
        return dto;
    }
}
