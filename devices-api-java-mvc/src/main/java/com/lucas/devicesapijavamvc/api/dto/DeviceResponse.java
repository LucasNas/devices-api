package com.lucas.devicesapijavamvc.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceResponse(
        Long id,
        UUID externalId,
        String name,
        String brand,
        String state,
        OffsetDateTime creationTime
) {}
