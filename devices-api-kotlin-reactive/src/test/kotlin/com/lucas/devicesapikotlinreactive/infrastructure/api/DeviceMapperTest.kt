package com.lucas.devicesapikotlinreactive.infrastructure.api

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.infrastructure.api.dto.DeviceRequest
import com.lucas.devicesapikotlinreactive.infrastructure.api.mapper.DeviceMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class DeviceMapperTest {

    @Test
    @DisplayName("toDomain() maps DeviceRequest to a Device instance correctly")
    fun toDomainShouldMapCorrectly() {
        val req = DeviceRequest(
            name = "Phone",
            brand = "Apple",
            state = DeviceState.AVAILABLE
        )

        val device = DeviceMapper.toDomain(req)

        assertThat(device.id).isNull()
        assertThat(device.name).isEqualTo(req.name)
        assertThat(device.brand).isEqualTo(req.brand)
        assertThat(device.state).isEqualTo(req.state)
        assertThat(device.externalId).isNotNull()
        assertThat(device.creationTime).isNotNull()
    }

    @Test
    @DisplayName("toResponse() maps Device to DeviceResponse correctly")
    fun toResponseShouldMapCorrectly() {
        val externalId = UUID.randomUUID()
        val creationTime = OffsetDateTime.now().minusHours(2)

        val device = Device(
            id = 1L,
            externalId = externalId,
            name = "Tablet",
            brand = "Samsung",
            state = DeviceState.RETIRED,
            creationTime = creationTime
        )

        val resp = DeviceMapper.toResponse(device)

        assertThat(resp.id).isEqualTo(1L)
        assertThat(resp.externalId).isEqualTo(externalId)
        assertThat(resp.name).isEqualTo("Tablet")
        assertThat(resp.brand).isEqualTo("Samsung")
        assertThat(resp.state).isEqualTo(DeviceState.RETIRED.name)
        assertThat(resp.creationTime).isEqualTo(creationTime)
    }
}
