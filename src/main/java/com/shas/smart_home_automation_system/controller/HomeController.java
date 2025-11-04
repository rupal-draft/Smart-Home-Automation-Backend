package com.shas.smart_home_automation_system.controller;

import com.shas.smart_home_automation_system.dto.HomeDto;
import com.shas.smart_home_automation_system.service.HomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/homes")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<List<HomeDto>> getUserHomes() {
        List<HomeDto> homes = homeService.getUserHomes();
        return ResponseEntity.ok(homes);
    }

    @GetMapping("/{homeId}")
    public ResponseEntity<HomeDto> getHomeById(@PathVariable Long homeId) {
        HomeDto home = homeService.getHomeById(homeId);
        return ResponseEntity.ok(home);
    }

    @PostMapping
    public ResponseEntity<HomeDto> createHome(@Valid @RequestBody HomeDto homeDto) {
        HomeDto createdHome = homeService.createHome(homeDto);
        return ResponseEntity.ok(createdHome);
    }

    @PutMapping("/{homeId}")
    public ResponseEntity<HomeDto> updateHome(
            @PathVariable Long homeId,
            @Valid @RequestBody HomeDto homeDto) {
        HomeDto updatedHome = homeService.updateHome(homeId, homeDto);
        return ResponseEntity.ok(updatedHome);
    }

    @DeleteMapping("/{homeId}")
    public ResponseEntity<Void> deleteHome(@PathVariable Long homeId) {
        homeService.deleteHome(homeId);
        return ResponseEntity.noContent().build();
    }
}

