package com.shas.smart_home_automation_system.service;

import com.shas.smart_home_automation_system.dto.RoomDto;

import java.util.List;

public interface RoomService {

    List<RoomDto> getUserRooms();

    List<RoomDto> getHomeRooms(Long homeId);

    RoomDto getRoomById(Long roomId);

    RoomDto createRoom(RoomDto roomDto);

    RoomDto updateRoom(Long roomId, RoomDto roomDto);

    void deleteRoom(Long roomId);
}