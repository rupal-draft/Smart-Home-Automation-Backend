package com.shas.smart_home_automation_system.controller;

import com.shas.smart_home_automation_system.dto.RoomDto;
import com.shas.smart_home_automation_system.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<RoomDto>> getUserRooms() {
        List<RoomDto> rooms = roomService.getUserRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/home/{homeId}")
    public ResponseEntity<List<RoomDto>> getHomeRooms(@PathVariable Long homeId) {
        List<RoomDto> rooms = roomService.getHomeRooms(homeId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long roomId) {
        RoomDto room = roomService.getRoomById(roomId);
        return ResponseEntity.ok(room);
    }

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody RoomDto roomDto) {
        RoomDto createdRoom = roomService.createRoom(roomDto);
        return ResponseEntity.ok(createdRoom);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomDto> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomDto roomDto) {
        RoomDto updatedRoom = roomService.updateRoom(roomId, roomDto);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}

