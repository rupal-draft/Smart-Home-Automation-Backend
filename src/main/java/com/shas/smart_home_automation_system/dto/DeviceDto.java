package com.shas.smart_home_automation_system.dto;

import com.shas.smart_home_automation_system.enums.DeviceStatus;
import com.shas.smart_home_automation_system.enums.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDto {
    private Long id;
    private String name;
    private String deviceId;
    private DeviceType type;
    private DeviceStatus status;
    private Long homeId;
    private Long roomId;
    private String manufacturer;
    private String model;
    private Double powerConsumption;
    private LocalDateTime createdAt;
}
