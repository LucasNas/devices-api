package com.lucas.devicesapikotlinreactive.application.service

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.domain.port.DeviceRepositoryPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class DeviceService(
    private val repo: DeviceRepositoryPort
) {

    fun create(newDevice: Device): Mono<Device> =
        repo.save(newDevice)

    fun get(id: Long): Mono<Device> =
        repo.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Device $id not found")))

    fun all(): Flux<Device> =
        repo.findAll()

    fun byBrand(brand: String): Flux<Device> =
        repo.findByBrand(brand)

    fun byState(state: DeviceState): Flux<Device> =
        repo.findByState(state)

    fun updateFull(id: Long, incoming: Device): Mono<Device> =
        get(id).flatMap { existing ->
            val updated = existing.copy(
                name = incoming.name,
                brand = incoming.brand,
                state = incoming.state
            )
            repo.save(updated)
        }

    fun delete(id: Long): Mono<Void> =
        repo.delete(id)

    class NotFoundException(message: String) : RuntimeException(message)
}
