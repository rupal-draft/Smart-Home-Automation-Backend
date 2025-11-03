package com.shas.smart_home_automation_system.repository;

import com.shas.smart_home_automation_system.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHomeIdAndHomeUserId(Long homeId, Long userId);

    Optional<Room> findByIdAndHomeUserId(Long id, Long userId);

    List<Room> findByHomeUserId(Long userId);
}
