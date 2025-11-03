package com.shas.smart_home_automation_system.dto;

import com.shas.smart_home_automation_system.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto {
    private Long id;
    private String name;
    private String description;
    private RoomType type;
    private Long homeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
