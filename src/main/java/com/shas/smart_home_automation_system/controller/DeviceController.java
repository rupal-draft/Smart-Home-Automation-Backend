package com.shas.smart_home_automation_system.controller;

import com.shas.smart_home_automation_system.dto.DeviceDto;
import com.shas.smart_home_automation_system.enums.DeviceStatus;
import com.shas.smart_home_automation_system.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<DeviceDto>> getUserDevices() {
        return ResponseEntity.ok(deviceService.getUserDevices());
    }

    @GetMapping("/home/{homeId}")
    public ResponseEntity<List<DeviceDto>> getHomeDevices(@PathVariable Long homeId) {
        return ResponseEntity.ok(deviceService.getHomeDevices(homeId));
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody DeviceDto deviceDto) {
        return ResponseEntity.ok(deviceService.createDevice(deviceDto));
    }

    @PatchMapping("/{deviceId}/status")
    public ResponseEntity<DeviceDto> updateDeviceStatus(
            @PathVariable Long deviceId,
            @RequestParam DeviceStatus status
    ) {
        return ResponseEntity.ok(deviceService.updateDeviceStatus(deviceId, status));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/home/{homeId}/power-consumption")
    public ResponseEntity<Double> getHomePowerConsumption(@PathVariable Long homeId) {
        return ResponseEntity.ok(deviceService.getHomePowerConsumption(homeId));
    }
}

