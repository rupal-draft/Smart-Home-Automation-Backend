package com.shas.smart_home_automation_system.service.implementation;

import com.shas.smart_home_automation_system.dto.RoomDto;
import com.shas.smart_home_automation_system.entity.Home;
import com.shas.smart_home_automation_system.entity.Room;
import com.shas.smart_home_automation_system.enums.RoomType;
import com.shas.smart_home_automation_system.exceptions.ResourceNotFoundException;
import com.shas.smart_home_automation_system.repository.HomeRepository;
import com.shas.smart_home_automation_system.repository.RoomRepository;
import com.shas.smart_home_automation_system.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "rooms")
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HomeRepository homeRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'userRooms:' + #userId")
    public List<RoomDto> getUserRooms(Long userId) {
        log.info("Fetching rooms for userId {}", userId);
        return roomRepository.findByHomeUserId(userId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'homeRooms:' + #homeId + ':user:' + #userId")
    public List<RoomDto> getHomeRooms(Long homeId, Long userId) {
        log.info("Fetching rooms for homeId {} and userId {}", homeId, userId);
        return roomRepository.findByHomeIdAndHomeUserId(homeId, userId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'room:' + #roomId + ':user:' + #userId")
    public RoomDto getRoomById(Long roomId, Long userId) {
        log.info("Fetching room {} for user {}", roomId, userId);
        Room room = roomRepository.findByIdAndHomeUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        return convertToDto(room);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'userRooms:' + #userId", allEntries = true),
            @CacheEvict(key = "'homeRooms:' + #roomDto.homeId + ':user:' + #userId", allEntries = true)
    })
    public RoomDto createRoom(RoomDto roomDto, Long userId) {
        log.info("Creating room '{}' for user {}", roomDto.getName(), userId);
        Home home = homeRepository.findByIdAndUserId(roomDto.getHomeId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found or access denied"));

        Room room = new Room();
        room.setName(roomDto.getName());
        room.setDescription(roomDto.getDescription());
        room.setType(RoomType.valueOf(roomDto.getType()));
        room.setHome(home);

        Room savedRoom = roomRepository.save(room);
        return convertToDto(savedRoom);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'room:' + #roomId + ':user:' + #userId"),
            @CacheEvict(key = "'homeRooms:*'"), // wildcard pattern, useful if multiple rooms in a home are cached
            @CacheEvict(key = "'userRooms:' + #userId")
    })
    public RoomDto updateRoom(Long roomId, RoomDto roomDto, Long userId) {
        log.info("Updating room {} for user {}", roomId, userId);
        Room room = roomRepository.findByIdAndHomeUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        room.setName(roomDto.getName());
        room.setDescription(roomDto.getDescription());
        room.setType(RoomType.valueOf(roomDto.getType()));

        Room updatedRoom = roomRepository.save(room);
        return convertToDto(updatedRoom);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'room:' + #roomId + ':user:' + #userId"),
            @CacheEvict(key = "'homeRooms:*'"),
            @CacheEvict(key = "'userRooms:' + #userId")
    })
    public void deleteRoom(Long roomId, Long userId) {
        log.info("Deleting room {} for user {}", roomId, userId);
        Room room = roomRepository.findByIdAndHomeUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        roomRepository.delete(room);
    }

    private RoomDto convertToDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setDescription(room.getDescription());
        dto.setType(room.getType().name());
        dto.setHomeId(room.getHome().getId());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());
        return dto;
    }
}
