package com.shas.smart_home_automation_system.service;

import com.shas.smart_home_automation_system.dto.RoomDto;

import java.util.List;

public interface RoomService {

    List<RoomDto> getUserRooms(Long userId);

    List<RoomDto> getHomeRooms(Long homeId, Long userId);

    RoomDto getRoomById(Long roomId, Long userId);

    RoomDto createRoom(RoomDto roomDto, Long userId);

    RoomDto updateRoom(Long roomId, RoomDto roomDto, Long userId);

    void deleteRoom(Long roomId, Long userId);
}