package com.lucas.devicesapijavamvc.api;

import com.lucas.devicesapijavamvc.api.dto.DeviceRequest;
import com.lucas.devicesapijavamvc.api.dto.DeviceResponse;
import com.lucas.devicesapijavamvc.domain.Device;
import com.lucas.devicesapijavamvc.domain.DeviceState;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceMapperTest {

    @Test
    void toDomain_shouldCreateNewDeviceFromRequest() {
        DeviceRequest req = new DeviceRequest("iPhone 15", "Apple", DeviceState.AVAILABLE);

        Device device = DeviceMapper.toNewEntity(req);

        assertThat(device.getId()).isNull();
        assertThat(device.getName()).isEqualTo("iPhone 15");
        assertThat(device.getBrand()).isEqualTo("Apple");
        assertThat(device.getState()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(device.getExternalId()).isNotNull();
        assertThat(device.getCreationTime()).isNotNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        UUID externalId = UUID.randomUUID();
        OffsetDateTime created = OffsetDateTime.now();

        Device device = Device.fromEvent(
                externalId,
                "Galaxy S24",
                "Samsung",
                DeviceState.RETIRED,
                created
        );

        DeviceResponse response = DeviceMapper.toResponse(device);

        assertThat(response.externalId()).isEqualTo(externalId);
        assertThat(response.name()).isEqualTo("Galaxy S24");
        assertThat(response.brand()).isEqualTo("Samsung");
        assertThat(response.state()).isEqualTo(DeviceState.RETIRED.name());
        assertThat(response.creationTime()).isEqualTo(created);
    }
}
