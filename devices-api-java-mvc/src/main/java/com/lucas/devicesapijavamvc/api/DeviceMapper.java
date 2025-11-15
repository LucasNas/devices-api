package com.lucas.devicesapijavamvc.api;

import com.lucas.devicesapijavamvc.api.dto.DeviceRequest;
import com.lucas.devicesapijavamvc.api.dto.DeviceResponse;
import com.lucas.devicesapijavamvc.domain.Device;

public final class DeviceMapper {

    private DeviceMapper() {}

    public static Device toNewEntity(DeviceRequest req) {
        return Device.createNew(req.name(), req.brand(), req.state());
    }

    public static DeviceResponse toResponse(Device d) {
        return new DeviceResponse(
                d.getId(),
                d.getExternalId(),
                d.getName(),
                d.getBrand(),
                d.getState().name(),
                d.getCreationTime()
        );
    }
}
