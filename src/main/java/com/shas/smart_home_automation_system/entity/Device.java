package com.shas.smart_home_automation_system.entity;

import com.shas.smart_home_automation_system.enums.DeviceStatus;
import com.shas.smart_home_automation_system.enums.DeviceType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;



@Entity
@Table(
        name = "devices",
        indexes = {
                @Index(name = "idx_device_device_id", columnList = "deviceId"),
                @Index(name = "idx_device_type", columnList = "type"),
                @Index(name = "idx_device_status", columnList = "status"),
                @Index(name = "idx_device_home_id", columnList = "home_id"),
                @Index(name = "idx_device_room_id", columnList = "room_id"),
                @Index(name = "idx_device_ip_address", columnList = "ipAddress")
        }
)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status = DeviceStatus.OFFLINE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_id", nullable = false)
    private Home home;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private String manufacturer;
    private String model;
    private String ipAddress;
    private Double powerConsumption = 0.0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}

