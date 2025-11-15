package com.lucas.devicesapikotlinreactive.infrastructure.database

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.domain.port.DeviceRepositoryPort
import com.lucas.devicesapikotlinreactive.infrastructure.database.entity.DeviceEntity
import com.lucas.devicesapikotlinreactive.infrastructure.database.repository.DeviceR2dbcRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class DeviceRepositoryAdapter(
    private val repo: DeviceR2dbcRepository
) : DeviceRepositoryPort {

    override fun save(device: Device): Mono<Device> =
        repo.save(device.toEntity()).map { it.toDomain() }

    override fun findById(id: Long): Mono<Device> =
        repo.findById(id).map { it.toDomain() }

    override fun findAll(): Flux<Device> =
        repo.findAll().map { it.toDomain() }

    override fun findByBrand(brand: String): Flux<Device> =
        repo.findByBrand(brand).map { it.toDomain() }

    override fun findByState(state: DeviceState): Flux<Device> =
        repo.findByState(state).map { it.toDomain() }

    override fun delete(id: Long): Mono<Void> =
        repo.deleteById(id)

    override fun findByExternalId(externalId: UUID): Mono<Device> =
        repo.findByExternalId(externalId).map { it.toDomain() }
}

private fun Device.toEntity() =
    DeviceEntity(
        id = this.id,
        externalId = this.externalId,
        name = this.name,
        brand = this.brand,
        state = this.state,
        creationTime = this.creationTime
    )

private fun DeviceEntity.toDomain() =
    Device(
        id = this.id,
        externalId = this.externalId,
        name = this.name,
        brand = this.brand,
        state = this.state,
        creationTime = this.creationTime
    )
