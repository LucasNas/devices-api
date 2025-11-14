package com.lucas.devicesapikotlinreactive.infrastructure.api.dto

import java.time.OffsetDateTime
import java.util.UUID

data class DeviceResponse(
    val id: Long,
    val externalId: UUID,
    val name: String,
    val brand: String,
    val state: String,
    val creationTime: OffsetDateTime
)
