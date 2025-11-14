package com.lucas.devicesapijavamvc.api;

import com.lucas.devicesapijavamvc.api.dto.DeviceRequest;
import com.lucas.devicesapijavamvc.api.dto.DeviceResponse;
import com.lucas.devicesapijavamvc.domain.Device;
import com.lucas.devicesapijavamvc.domain.DeviceState;
import com.lucas.devicesapijavamvc.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse create(@Valid @RequestBody DeviceRequest req) {
        Device entity = DeviceMapper.toNewEntity(req);
        return DeviceMapper.toResponse(service.create(entity));
    }

    @GetMapping("/{id}")
    public DeviceResponse get(@PathVariable Long id) {
        return DeviceMapper.toResponse(service.get(id));
    }

    @GetMapping
    public List<DeviceResponse> list(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) DeviceState state
    ) {
        if (brand != null) {
            return service.byBrand(brand).stream().map(DeviceMapper::toResponse).toList();
        }
        if (state != null) {
            return service.byState(state).stream().map(DeviceMapper::toResponse).toList();
        }
        return service.all().stream().map(DeviceMapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    public DeviceResponse fullUpdate(@PathVariable Long id,
                                     @Valid @RequestBody DeviceRequest req) {
        Device incoming = DeviceMapper.toNewEntity(req);
        return DeviceMapper.toResponse(service.updateFull(id, incoming));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
