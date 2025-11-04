package com.shas.smart_home_automation_system.repository;

import com.shas.smart_home_automation_system.entity.Room;
import com.shas.smart_home_automation_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHomeIdAndHomeUser(Long homeId, User user);

    Optional<Room> findByIdAndHomeUser(Long id, User user);

    List<Room> findByHomeUser(User user);
}
