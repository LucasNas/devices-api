package com.lucas.devicesapikotlinreactive.infrastructure.api

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.infrastructure.api.dto.DeviceRequest
import com.lucas.devicesapikotlinreactive.infrastructure.api.dto.DeviceResponse

object DeviceMapper {

    fun toDomain(req: DeviceRequest): Device =
        Device.create(
            name = req.name,
            brand = req.brand,
            state = req.state
        )

    fun toResponse(device: Device): DeviceResponse =
        DeviceResponse(
            id = device.id ?: 0L,
            externalId = device.externalId,
            name = device.name,
            brand = device.brand,
            state = device.state.name,
            creationTime = device.creationTime
        )
}
