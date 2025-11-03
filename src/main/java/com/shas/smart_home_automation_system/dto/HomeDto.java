package com.shas.smart_home_automation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeDto {
    private Long id;
    private String name;
    private String address;
    private String timezone;
    private Long userId;
    private LocalDateTime createdAt;
}
