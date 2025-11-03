package com.shas.smart_home_automation_system.controller;

import com.shas.smart_home_automation_system.dto.HomeDto;
import com.shas.smart_home_automation_system.service.HomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/homes")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<List<HomeDto>> getUserHomes(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<HomeDto> homes = homeService.getUserHomes(userId);
        return ResponseEntity.ok(homes);
    }

    @GetMapping("/{homeId}")
    public ResponseEntity<HomeDto> getHomeById(
            @PathVariable Long homeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        HomeDto home = homeService.getHomeById(homeId, userId);
        return ResponseEntity.ok(home);
    }

    @PostMapping
    public ResponseEntity<HomeDto> createHome(
            @Valid @RequestBody HomeDto homeDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        HomeDto createdHome = homeService.createHome(homeDto, userId);
        return ResponseEntity.ok(createdHome);
    }

    @PutMapping("/{homeId}")
    public ResponseEntity<HomeDto> updateHome(
            @PathVariable Long homeId,
            @Valid @RequestBody HomeDto homeDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        HomeDto updatedHome = homeService.updateHome(homeId, homeDto, userId);
        return ResponseEntity.ok(updatedHome);
    }

    @DeleteMapping("/{homeId}")
    public ResponseEntity<Void> deleteHome(
            @PathVariable Long homeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        homeService.deleteHome(homeId, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // Implementation depends on how you store user ID in UserDetails
        // This is a placeholder - you need to implement based on your UserDetails implementation
        return 1L;
    }
}
