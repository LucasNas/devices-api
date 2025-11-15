package com.lucas.devicesapikotlinreactive.domain.model

import com.lucas.devicesapikotlinreactive.infrastructure.messaging.DeviceEvent
import com.lucas.devicesapikotlinreactive.infrastructure.messaging.toEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class DeviceEventMappingTest {

    @Test
    @DisplayName("toEvent() maps Device to DeviceEvent with the given origin")
    fun toEventShouldMapDeviceToDeviceEvent() {
        val externalId = UUID.randomUUID()
        val creationTime = OffsetDateTime.now().minusMinutes(30)

        val device = Device(
            id = 1L,
            externalId = externalId,
            name = "Tablet",
            brand = "Samsung",
            state = DeviceState.RETIRED,
            creationTime = creationTime
        )

        val event: DeviceEvent = device.toEvent(origin = "JAVA")

        assertThat(event.externalId).isEqualTo(externalId)
        assertThat(event.name).isEqualTo("Tablet")
        assertThat(event.brand).isEqualTo("Samsung")
        assertThat(event.state).isEqualTo(DeviceState.RETIRED.name)
        assertThat(event.creationTime).isEqualTo(creationTime)
        assertThat(event.origin).isEqualTo("JAVA")
    }
}
