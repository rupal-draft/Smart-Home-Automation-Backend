package com.shas.smart_home_automation_system.controller;

import com.shas.smart_home_automation_system.dto.RoomDto;
import com.shas.smart_home_automation_system.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<RoomDto>> getUserRooms(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<RoomDto> rooms = roomService.getUserRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/home/{homeId}")
    public ResponseEntity<List<RoomDto>> getHomeRooms(
            @PathVariable Long homeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<RoomDto> rooms = roomService.getHomeRooms(homeId, userId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        RoomDto room = roomService.getRoomById(roomId, userId);
        return ResponseEntity.ok(room);
    }

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(
            @Valid @RequestBody RoomDto roomDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        RoomDto createdRoom = roomService.createRoom(roomDto, userId);
        return ResponseEntity.ok(createdRoom);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomDto> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomDto roomDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        RoomDto updatedRoom = roomService.updateRoom(roomId, roomDto, userId);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        roomService.deleteRoom(roomId, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // Implementation depends on how you store user ID in UserDetails
        // This is a placeholder - you need to implement based on your UserDetails implementation
        return 1L;
    }
}
