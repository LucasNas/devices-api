package com.lucas.devicesapikotlinreactive.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class DeviceTest {

    @Test
    @DisplayName("create() builds a new Device with generated externalId and creationTime")
    fun createShouldBuildNewDevice() {
        val device = Device.create(
            name = "iPhone 15",
            brand = "Apple",
            state = DeviceState.AVAILABLE
        )

        assertThat(device.id).isNull()
        assertThat(device.externalId).isNotNull()
        assertThat(device.name).isEqualTo("iPhone 15")
        assertThat(device.brand).isEqualTo("Apple")
        assertThat(device.state).isEqualTo(DeviceState.AVAILABLE)
        assertThat(device.creationTime).isNotNull()
    }

    @Test
    @DisplayName("fromEvent() builds a Device using the provided event data")
    fun fromEventShouldBuildDeviceFromEventData() {
        val externalId = UUID.randomUUID()
        val creationTime = OffsetDateTime.now().minusDays(1)

        val device = Device.fromEvent(
            externalId = externalId,
            name = "Galaxy S24",
            brand = "Samsung",
            state = DeviceState.IN_USE,
            creationTime = creationTime
        )

        assertThat(device.id).isNull()
        assertThat(device.externalId).isEqualTo(externalId)
        assertThat(device.name).isEqualTo("Galaxy S24")
        assertThat(device.brand).isEqualTo("Samsung")
        assertThat(device.state).isEqualTo(DeviceState.IN_USE)
        assertThat(device.creationTime).isEqualTo(creationTime)
    }
}
