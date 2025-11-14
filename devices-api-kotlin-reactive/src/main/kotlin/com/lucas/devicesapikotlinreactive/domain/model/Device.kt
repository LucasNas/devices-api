package com.lucas.devicesapikotlinreactive.domain.model

import java.time.OffsetDateTime
import java.util.UUID

data class Device(
    val id: Long? = null,
    val externalId: UUID,
    val name: String,
    val brand: String,
    val state: DeviceState,
    val creationTime: OffsetDateTime
) {
    companion object {
        fun create(name: String, brand: String, state: DeviceState): Device =
            Device(
                id = null,
                externalId = UUID.randomUUID(),
                name = name,
                brand = brand,
                state = state,
                creationTime = OffsetDateTime.now()
            )
    }
}
