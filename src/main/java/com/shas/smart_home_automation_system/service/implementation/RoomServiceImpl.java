package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.RoomDto;
import com.shas.smart_home_automation_system.entity.Home;
import com.shas.smart_home_automation_system.entity.Room;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.HomeRepository;
import com.shas.smart_home_automation_system.repository.RoomRepository;
import com.shas.smart_home_automation_system.service.RoomService;
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
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HomeRepository homeRepository;
    private final CacheService cacheService;
    private final ModelMapper modelMapper;

    private static final String CACHE_NAME = "rooms";
    private static final long CACHE_TTL = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getUserRooms(Long userId) {
        String cacheKey = "userRooms:" + userId;

        @SuppressWarnings("unchecked")
        List<RoomDto> cachedRooms = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedRooms != null) return cachedRooms;

        List<RoomDto> rooms = roomRepository.findByHomeUserId(userId)
                .stream()
                .map(this::convertToDto)
                .toList();
        cacheService.put(CACHE_NAME, cacheKey, rooms, CACHE_TTL, TIME_UNIT);
        return rooms;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getHomeRooms(Long homeId, Long userId) {
        String cacheKey = "homeRooms:" + homeId + ":user:" + userId;

        @SuppressWarnings("unchecked")
        List<RoomDto> cachedRooms = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedRooms != null) return cachedRooms;

        List<RoomDto> rooms = roomRepository.findByHomeIdAndHomeUserId(homeId, userId)
                .stream()
                .map(this::convertToDto)
                .toList();
        cacheService.put(CACHE_NAME, cacheKey, rooms, CACHE_TTL, TIME_UNIT);
        return rooms;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long roomId, Long userId) {
        String cacheKey = "room:" + roomId + ":user:" + userId;
        RoomDto cachedRoom = cacheService.get(CACHE_NAME, cacheKey, RoomDto.class);
        if (cachedRoom != null) return cachedRoom;

        Room room = roomRepository.findByIdAndHomeUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        RoomDto roomDto = convertToDto(room);
        cacheService.put(CACHE_NAME, cacheKey, roomDto, CACHE_TTL, TIME_UNIT);
        return roomDto;
    }

    @Override
    public RoomDto createRoom(RoomDto roomDto, Long userId) {
        Home home = homeRepository.findByIdAndUserId(roomDto.getHomeId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found or access denied"));

        Room room = new Room();
        room.setName(roomDto.getName());
        room.setDescription(roomDto.getDescription());
        room.setType(roomDto.getType());
        room.setHome(home);

        Room savedRoom = roomRepository.save(room);
        cacheService.evict(CACHE_NAME, "userRooms:" + userId);
        cacheService.evict(CACHE_NAME, "homeRooms:" + roomDto.getHomeId() + ":user:" + userId);
        return convertToDto(savedRoom);
    }

    @Override
    public RoomDto updateRoom(Long roomId, RoomDto roomDto, Long userId) {
        Room room = roomRepository.findByIdAndHomeUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        room.setName(roomDto.getName());
        room.setDescription(roomDto.getDescription());
        room.setType(roomDto.getType());

        Room updatedRoom = roomRepository.save(room);
        RoomDto updatedDto = convertToDto(updatedRoom);

        cacheService.put(CACHE_NAME, "room:" + roomId + ":user:" + userId, updatedDto, CACHE_TTL, TIME_UNIT);
        cacheService.evict(CACHE_NAME, "userRooms:" + userId);
        cacheService.evict(CACHE_NAME, "homeRooms:" + room.getHome().getId() + ":user:" + userId);
        return updatedDto;
    }

    @Override
    public void deleteRoom(Long roomId, Long userId) {
        Room room = roomRepository.findByIdAndHomeUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        roomRepository.delete(room);
        cacheService.evict(CACHE_NAME, "room:" + roomId + ":user:" + userId);
        cacheService.evict(CACHE_NAME, "userRooms:" + userId);
        cacheService.evict(CACHE_NAME, "homeRooms:" + room.getHome().getId() + ":user:" + userId);
    }

    private RoomDto convertToDto(Room room) {
        return modelMapper.map(room, RoomDto.class);
    }
}
