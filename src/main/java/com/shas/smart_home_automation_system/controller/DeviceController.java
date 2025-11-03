package com.shas.smart_home_automation_system.controller;

import com.shas.smart_home_automation_system.dto.DeviceDto;
import com.shas.smart_home_automation_system.enums.DeviceStatus;
import com.shas.smart_home_automation_system.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<DeviceDto>> getUserDevices(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<DeviceDto> devices = deviceService.getUserDevices(userId);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/home/{homeId}")
    public ResponseEntity<List<DeviceDto>> getHomeDevices(
            @PathVariable Long homeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<DeviceDto> devices = deviceService.getHomeDevices(homeId, userId);
        return ResponseEntity.ok(devices);
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(
            @Valid @RequestBody DeviceDto deviceDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        DeviceDto createdDevice = deviceService.createDevice(deviceDto, userId);
        return ResponseEntity.ok(createdDevice);
    }

    @PatchMapping("/{deviceId}/status")
    public ResponseEntity<DeviceDto> updateDeviceStatus(
            @PathVariable Long deviceId,
            @RequestParam DeviceStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        DeviceDto updatedDevice = deviceService.updateDeviceStatus(deviceId, status, userId);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable Long deviceId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        deviceService.deleteDevice(deviceId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/home/{homeId}/power-consumption")
    public ResponseEntity<Double> getHomePowerConsumption(
            @PathVariable Long homeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        Double powerConsumption = deviceService.getHomePowerConsumption(homeId, userId);
        return ResponseEntity.ok(powerConsumption);
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // Implementation to extract user ID from UserDetails
        return 1L; // Placeholder - implement based on your UserDetails implementation
    }
}
