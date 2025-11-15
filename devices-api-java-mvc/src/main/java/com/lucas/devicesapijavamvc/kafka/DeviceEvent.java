package com.lucas.devicesapijavamvc.kafka;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceEvent(
        UUID externalId,
        String name,
        String brand,
        String state,
        OffsetDateTime creationTime,
        String origin // "JAVA" or "KOTLIN"
) {}
