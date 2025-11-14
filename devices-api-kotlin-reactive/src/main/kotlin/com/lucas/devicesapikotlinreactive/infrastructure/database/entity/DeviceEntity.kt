package com.lucas.devicesapikotlinreactive.infrastructure.database.entity

import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.UUID

@Table("devices")
data class DeviceEntity(
    @Id
    val id: Long? = null,

    @Column("external_id")
    val externalId: UUID,

    val name: String,
    val brand: String,
    val state: DeviceState,

    @Column("creation_time")
    val creationTime: OffsetDateTime
)