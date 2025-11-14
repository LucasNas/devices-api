package com.lucas.devicesapijavamvc.repository;

import com.lucas.devicesapijavamvc.domain.Device;
import com.lucas.devicesapijavamvc.domain.DeviceState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByExternalId(UUID externalId);

    List<Device> findByBrand(String brand);

    List<Device> findByState(DeviceState state);
}
