package com.lucas.devicesapijavamvc.api.dto;

import com.lucas.devicesapijavamvc.domain.DeviceState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceRequest(
        @NotBlank String name,
        @NotBlank String brand,
        @NotNull DeviceState state
) {}
