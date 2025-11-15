package com.lucas.devicesapijavamvc.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceTest {

    @Test
    void createNew_shouldGenerateExternalIdAndCreationTime() {
        Device device = Device.createNew("iPhone 15", "Apple", DeviceState.AVAILABLE);

        assertThat(device.getId()).isNull();
        assertThat(device.getExternalId()).isNotNull();
        assertThat(device.getName()).isEqualTo("iPhone 15");
        assertThat(device.getBrand()).isEqualTo("Apple");
        assertThat(device.getState()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(device.getCreationTime()).isNotNull();
    }

    @Test
    void fromEvent_shouldUseAllEventFields() {
        UUID externalId = UUID.randomUUID();
        OffsetDateTime created = OffsetDateTime.now().minusHours(2);

        Device device = Device.fromEvent(
                externalId,
                "Galaxy S24",
                "Samsung",
                DeviceState.IN_USE,
                created
        );

        assertThat(device.getId()).isNull();
        assertThat(device.getExternalId()).isEqualTo(externalId);
        assertThat(device.getName()).isEqualTo("Galaxy S24");
        assertThat(device.getBrand()).isEqualTo("Samsung");
        assertThat(device.getState()).isEqualTo(DeviceState.IN_USE);
        assertThat(device.getCreationTime()).isEqualTo(created);
    }
}
