package com.lucas.devicesapikotlinreactive.infrastructure.messaging

import com.lucas.devicesapikotlinreactive.domain.model.Device
import java.time.OffsetDateTime
import java.util.UUID

data class DeviceEvent(
    val externalId: UUID,
    val name: String,
    val brand: String,
    val state: String,
    val creationTime: OffsetDateTime,
    val origin: String // "JAVA" or "KOTLIN"
)

fun Device.toEvent(origin: String): DeviceEvent =
    DeviceEvent(
        externalId = this.externalId,
        name = this.name,
        brand = this.brand,
        state = this.state.name,
        creationTime = this.creationTime,
        origin = origin
    )
