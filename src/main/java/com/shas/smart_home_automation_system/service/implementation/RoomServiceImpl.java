package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.RoomDto;
import com.shas.smart_home_automation_system.entity.Home;
import com.shas.smart_home_automation_system.entity.Room;
import com.shas.smart_home_automation_system.entity.User;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.HomeRepository;
import com.shas.smart_home_automation_system.repository.RoomRepository;
import com.shas.smart_home_automation_system.service.RoomService;
import com.shas.smart_home_automation_system.util.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getUserRooms() {
        User user = getAuthenticatedUser();
        Long userId = user.getId();
        String cacheKey = "userRooms:" + userId;

        @SuppressWarnings("unchecked")
        List<RoomDto> cachedRooms = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedRooms != null) {
            log.info("Returning rooms from cache for userId: {}", userId);
            return cachedRooms;
        }

        log.info("Fetching rooms from DB for userId: {}", userId);
        List<RoomDto> rooms = roomRepository.findByHomeUser(user)
                .stream()
                .map(this::convertToDto)
                .toList();

        cacheService.put(CACHE_NAME, cacheKey, rooms, CACHE_TTL, TIME_UNIT);
        return rooms;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getHomeRooms(Long homeId) {
        User user = getAuthenticatedUser();
        Long userId = user.getId();
        String cacheKey = "homeRooms:" + homeId + ":user:" + userId;

        @SuppressWarnings("unchecked")
        List<RoomDto> cachedRooms = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedRooms != null) {
            log.info("Returning rooms from cache for homeId: {} and userId: {}", homeId, userId);
            return cachedRooms;
        }

        log.info("Fetching rooms from DB for homeId: {} and userId: {}", homeId, userId);
        List<RoomDto> rooms = roomRepository.findByHomeIdAndHomeUser(homeId, user)
                .stream()
                .map(this::convertToDto)
                .toList();

        cacheService.put(CACHE_NAME, cacheKey, rooms, CACHE_TTL, TIME_UNIT);
        return rooms;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long roomId) {
        User user = getAuthenticatedUser();
        Long userId = user.getId();
        String cacheKey = "room:" + roomId + ":user:" + userId;

        RoomDto cachedRoom = cacheService.get(CACHE_NAME, cacheKey, RoomDto.class);
        if (cachedRoom != null) {
            log.info("Returning room {} for user {} from cache", roomId, userId);
            return cachedRoom;
        }

        log.info("Fetching room {} for user {} from DB", roomId, userId);
        Room room = roomRepository.findByIdAndHomeUser(roomId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        RoomDto roomDto = convertToDto(room);
        cacheService.put(CACHE_NAME, cacheKey, roomDto, CACHE_TTL, TIME_UNIT);
        return roomDto;
    }

    @Override
    public RoomDto createRoom(RoomDto roomDto) {
        User user = getAuthenticatedUser();
        Long userId = user.getId();

        log.info("Creating new room for userId: {}", userId);
        Home home = homeRepository.findByIdAndUser(roomDto.getHomeId(), user)
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
    public RoomDto updateRoom(Long roomId, RoomDto roomDto) {
        User user = getAuthenticatedUser();
        Long userId = user.getId();

        log.info("Updating room {} for user {}", roomId, userId);
        Room room = roomRepository.findByIdAndHomeUser(roomId, user)
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
    public void deleteRoom(Long roomId) {
        User user = getAuthenticatedUser();
        Long userId = user.getId();

        log.info("Deleting room {} for user {}", roomId, userId);
        Room room = roomRepository.findByIdAndHomeUser(roomId, user)
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
