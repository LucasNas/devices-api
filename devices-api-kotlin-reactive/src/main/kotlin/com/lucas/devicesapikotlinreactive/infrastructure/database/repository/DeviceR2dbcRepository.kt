package com.lucas.devicesapikotlinreactive.infrastructure.database.repository

import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.infrastructure.database.entity.DeviceEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface DeviceR2dbcRepository : ReactiveCrudRepository<DeviceEntity, Long> {

    fun findByBrand(brand: String): Flux<DeviceEntity>

    fun findByState(state: DeviceState): Flux<DeviceEntity>

    fun findByExternalId(externalId: UUID): Mono<DeviceEntity>
}
