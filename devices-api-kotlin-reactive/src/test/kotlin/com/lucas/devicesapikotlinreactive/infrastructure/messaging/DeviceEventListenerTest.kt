package com.lucas.devicesapikotlinreactive.infrastructure.messaging

import com.lucas.devicesapikotlinreactive.application.service.DeviceService
import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.util.*

class DeviceEventListenerTest {

    private val service = mockk<DeviceService>()
    private val listener = DeviceEventListener(service)

    @AfterEach
    fun tearDown() = clearAllMocks()

    @Test
    @DisplayName("onDeviceEvent() ignores events originated from Kotlin")
    fun onDeviceEventShouldIgnoreKotlinOrigin() {
        val event = DeviceEvent(
            externalId = UUID.randomUUID(),
            name = "Phone",
            brand = "Apple",
            state = DeviceState.AVAILABLE.name,
            creationTime = OffsetDateTime.now(),
            origin = "KOTLIN"
        )

        listener.onDeviceEvent(event)

        verify(exactly = 0) { service.upsertByExternalId(any()) }
    }

    @Test
    @DisplayName("onDeviceEvent() converts event to Device and calls upsertByExternalId for non-Kotlin origin")
    fun onDeviceEventShouldUpsertForNonKotlinOrigin() {
        val externalId = UUID.randomUUID()
        val creationTime = OffsetDateTime.now().minusHours(1)

        val event = DeviceEvent(
            externalId = externalId,
            name = "Galaxy S24",
            brand = "Samsung",
            state = DeviceState.IN_USE.name,
            creationTime = creationTime,
            origin = "JAVA"
        )

        val capturedDevice = slot<Device>()

        every { service.upsertByExternalId(capture(capturedDevice)) } answers {
            Mono.just(capturedDevice.captured)
        }

        listener.onDeviceEvent(event)

        verify(exactly = 1) { service.upsertByExternalId(any()) }

        val device = capturedDevice.captured
        assertThat(device.externalId).isEqualTo(externalId)
        assertThat(device.name).isEqualTo("Galaxy S24")
        assertThat(device.brand).isEqualTo("Samsung")
        assertThat(device.state).isEqualTo(DeviceState.IN_USE)
        assertThat(device.creationTime).isEqualTo(creationTime)
    }
}
