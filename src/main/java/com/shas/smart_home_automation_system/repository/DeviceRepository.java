package com.shas.smart_home_automation_system.repository;

import com.shas.smart_home_automation_system.entity.Device;
import com.shas.smart_home_automation_system.entity.User;
import com.shas.smart_home_automation_system.enums.DeviceStatus;
import com.shas.smart_home_automation_system.enums.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByHomeIdAndHomeUser(Long homeId, User user);

    List<Device> findByHomeUser(User user);

    List<Device> findByTypeAndStatus(DeviceType type, DeviceStatus status);

    @Query("SELECT SUM(d.powerConsumption) FROM Device d WHERE d.home.id = :homeId AND d.status = 'ONLINE'")
    Optional<Double> getTotalPowerConsumptionByHome(@Param("homeId") Long homeId);

    Optional<Device> findByDeviceId(String deviceId);
}