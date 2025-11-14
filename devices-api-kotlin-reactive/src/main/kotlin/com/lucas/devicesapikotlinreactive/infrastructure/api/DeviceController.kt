package com.lucas.devicesapikotlinreactive.infrastructure.api

import com.lucas.devicesapikotlinreactive.application.service.DeviceService
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.infrastructure.api.dto.DeviceRequest
import com.lucas.devicesapikotlinreactive.infrastructure.api.dto.DeviceResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/devices")
class DeviceController(
    private val service: DeviceService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: DeviceRequest): Mono<DeviceResponse> =
        service.create(DeviceMapper.toDomain(req))
            .map(DeviceMapper::toResponse)

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Mono<DeviceResponse> =
        service.get(id)
            .map(DeviceMapper::toResponse)

    @GetMapping
    fun list(
        @RequestParam(required = false) brand: String?,
        @RequestParam(required = false) state: DeviceState?
    ): Flux<DeviceResponse> =
        when {
            brand != null ->
                service.byBrand(brand).map(DeviceMapper::toResponse)

            state != null ->
                service.byState(state).map(DeviceMapper::toResponse)

            else ->
                service.all().map(DeviceMapper::toResponse)
        }

    @PutMapping("/{id}")
    fun fullUpdate(
        @PathVariable id: Long,
        @Valid @RequestBody req: DeviceRequest
    ): Mono<DeviceResponse> =
        service.updateFull(id, DeviceMapper.toDomain(req))
            .map(DeviceMapper::toResponse)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long): Mono<Void> =
        service.delete(id)
}
