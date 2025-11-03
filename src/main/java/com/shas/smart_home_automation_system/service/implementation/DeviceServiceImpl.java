package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.DeviceDto;
import com.shas.smart_home_automation_system.entity.Device;
import com.shas.smart_home_automation_system.entity.Home;
import com.shas.smart_home_automation_system.enums.DeviceStatus;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.DeviceRepository;
import com.shas.smart_home_automation_system.repository.HomeRepository;
import com.shas.smart_home_automation_system.service.DeviceService;
import com.shas.smart_home_automation_system.util.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final HomeRepository homeRepository;
    private final CacheService cacheService;
    private final ModelMapper modelMapper;

    private static final String DEVICES_USER_CACHE = "devices_user";
    private static final String DEVICES_HOME_CACHE = "devices_home";
    private static final String DEVICE_CACHE = "device";
    private static final String POWER_CONSUMPTION_CACHE = "power_consumption";

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDto> getUserDevices(Long userId) {
        log.info("Fetching all devices for userId: {}", userId);

        @SuppressWarnings("unchecked")
        List<DeviceDto> cachedDevices = cacheService.get(DEVICES_USER_CACHE, userId.toString(), List.class);
        if (cachedDevices != null) {
            log.info("Cache hit for user devices: {}", userId);
            return cachedDevices;
        }

        List<DeviceDto> devices = deviceRepository.findByHomeUserId(userId).stream()
                .map(this::convertToDto)
                .toList();

        cacheService.put(DEVICES_USER_CACHE, userId.toString(), devices, 30, TimeUnit.MINUTES);

        return devices;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDto> getHomeDevices(Long homeId, Long userId) {
        log.info("Fetching all devices for homeId: {} and userId: {}", homeId, userId);

        @SuppressWarnings("unchecked")
        List<DeviceDto> cachedDevices = cacheService.get(DEVICES_HOME_CACHE, homeId.toString(), List.class);
        if (cachedDevices != null) {
            log.info("Cache hit for home devices: {}", homeId);
            return cachedDevices;
        }

        List<DeviceDto> devices = deviceRepository.findByHomeIdAndHomeUserId(homeId, userId).stream()
                .map(this::convertToDto)
                .toList();

        cacheService.put(DEVICES_HOME_CACHE, homeId.toString(), devices, 30, TimeUnit.MINUTES);

        return devices;
    }

    @Override
    public DeviceDto createDevice(DeviceDto deviceDto, Long userId) {
        try {
            Home home = homeRepository.findByIdAndUserId(deviceDto.getHomeId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Home not found or access denied"));

            Device device = new Device();
            device.setName(deviceDto.getName());
            device.setDeviceId(deviceDto.getDeviceId());
            device.setType(deviceDto.getType());
            device.setStatus(DeviceStatus.OFFLINE);
            device.setHome(home);
            device.setManufacturer(deviceDto.getManufacturer());
            device.setModel(deviceDto.getModel());
            device.setPowerConsumption(deviceDto.getPowerConsumption());

            Device savedDevice = deviceRepository.save(device);
            log.info("Device created successfully with ID: {}", savedDevice.getId());

            cacheService.evictPattern(DEVICES_USER_CACHE, "*");
            cacheService.evictPattern(DEVICES_HOME_CACHE, "*");
            cacheService.evictPattern(POWER_CONSUMPTION_CACHE, "*");

            return convertToDto(savedDevice);

        } catch (Exception e) {
            log.error("Error creating device: {}", e.getMessage());
            throw e;
        }
    }

    @Override
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

            DeviceDto dto = convertToDto(updatedDevice);

            cacheService.put(DEVICE_CACHE, deviceId.toString(), dto, 30, TimeUnit.MINUTES);

            cacheService.evictPattern(DEVICES_USER_CACHE, "*");
            cacheService.evictPattern(DEVICES_HOME_CACHE, "*");
            cacheService.evictPattern(POWER_CONSUMPTION_CACHE, "*");

            return dto;
        } catch (Exception e) {
            log.error("Failed to update device {}: {}", deviceId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteDevice(Long deviceId, Long userId) {
        try {
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

            if (!device.getHome().getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Device not found or access denied");
            }

            deviceRepository.delete(device);
            log.info("Deleted device with ID: {}", deviceId);

            cacheService.evict(DEVICE_CACHE, deviceId.toString());
            cacheService.evictPattern(DEVICES_USER_CACHE, "*");
            cacheService.evictPattern(DEVICES_HOME_CACHE, "*");
            cacheService.evictPattern(POWER_CONSUMPTION_CACHE, "*");

        } catch (Exception e) {
            log.error("Failed to delete device {}: {}", deviceId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Double getHomePowerConsumption(Long homeId, Long userId) {
        log.info("Fetching power consumption for homeId: {}", homeId);

        Double cachedValue = cacheService.get(POWER_CONSUMPTION_CACHE, homeId.toString(), Double.class);
        if (cachedValue != null) {
            log.info("Cache hit for power consumption of homeId: {}", homeId);
            return cachedValue;
        }

        homeRepository.findByIdAndUserId(homeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found or access denied"));

        Double totalPower = deviceRepository.getTotalPowerConsumptionByHome(homeId).orElse(0.0);

        cacheService.put(POWER_CONSUMPTION_CACHE, homeId.toString(), totalPower, 10, TimeUnit.MINUTES);

        return totalPower;
    }

    private DeviceDto convertToDto(Device device) {
        return modelMapper.map(device, DeviceDto.class);
    }
}
