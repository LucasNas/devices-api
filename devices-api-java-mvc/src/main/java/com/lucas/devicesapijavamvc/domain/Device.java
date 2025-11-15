package com.lucas.devicesapijavamvc.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, updatable = false, unique = true)
    private UUID externalId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceState state;

    @Column(name = "creation_time", nullable = false, updatable = false)
    private OffsetDateTime creationTime;

    protected Device() {
    }

    private Device(UUID externalId,
                   String name,
                   String brand,
                   DeviceState state,
                   OffsetDateTime creationTime) {
        this.externalId = externalId;
        this.name = name;
        this.brand = brand;
        this.state = state;
        this.creationTime = creationTime;
    }

    public static Device createNew(String name, String brand, DeviceState state) {
        return new Device(
                UUID.randomUUID(),
                name,
                brand,
                state,
                OffsetDateTime.now()
        );
    }

    public static Device fromEvent(UUID externalId,
                                   String name,
                                   String brand,
                                   DeviceState state,
                                   OffsetDateTime creationTime) {
        return new Device(
                externalId,
                name,
                brand,
                state,
                creationTime
        );
    }

    public Long getId() {
        return id;
    }

    public UUID getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public DeviceState getState() {
        return state;
    }

    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }
}
