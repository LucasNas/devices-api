package com.lucas.devicesapikotlinreactive.infrastructure.api.dto

import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class DeviceRequest(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    val brand: String,

    @field:NotNull
    val state: DeviceState
)
