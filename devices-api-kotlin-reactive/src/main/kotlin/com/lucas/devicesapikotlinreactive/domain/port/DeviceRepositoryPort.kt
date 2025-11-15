package com.lucas.devicesapikotlinreactive.domain.port

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface DeviceRepositoryPort {

    fun save(device: Device): Mono<Device>

    fun findById(id: Long): Mono<Device>

    fun findAll(): Flux<Device>

    fun findByBrand(brand: String): Flux<Device>

    fun findByState(state: DeviceState): Flux<Device>

    fun findByExternalId(externalId: UUID): Mono<Device>

    fun delete(id: Long): Mono<Void>
}
