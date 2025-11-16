package com.lucas.devicesapikotlinreactive.application.service

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.domain.port.DeviceEventPublisherPort
import com.lucas.devicesapikotlinreactive.domain.port.DeviceRepositoryPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class DeviceService(
    private val repo: DeviceRepositoryPort,
    private val eventPublisher: DeviceEventPublisherPort?
) {

    fun create(name: String, brand: String, state: DeviceState): Mono<Device> {
        val device = Device.create(name, brand, state)

        return repo.save(device)
            .flatMap { saved ->
                eventPublisher
                    ?.publishCreated(saved)
                    ?.thenReturn(saved)
                    ?: Mono.just(saved)
            }
    }

    fun get(id: Long): Mono<Device> =
        repo.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Device $id not found")))

    fun all(): Flux<Device> = repo.findAll()

    fun byBrand(brand: String): Flux<Device> = repo.findByBrand(brand)

    fun byState(state: DeviceState): Flux<Device> = repo.findByState(state)

    fun updateFull(id: Long, incoming: Device): Mono<Device> =
        get(id).flatMap { existing ->
            // Domain rule: name/brand cannot be changed if IN_USE
            if (existing.state == DeviceState.IN_USE &&
                (existing.name != incoming.name || existing.brand != incoming.brand)
            ) {
                return@flatMap Mono.error<Device>(
                    IllegalStateException("Name and brand cannot be updated when device is in use")
                )
            }

            val updated = existing.copy(
                name = incoming.name,
                brand = incoming.brand,
                state = incoming.state
                // externalId and creationTime are preserved
            )

            repo.save(updated)
        }

    fun delete(id: Long): Mono<Void> =
        get(id).flatMap { existing ->
            // Domain rule: IN_USE devices cannot be deleted
            if (existing.state == DeviceState.IN_USE) {
                return@flatMap Mono.error<Void>(
                    IllegalStateException("Cannot delete device that is in use")
                )
            }
            repo.delete(existing.id
                ?: return@flatMap Mono.error(IllegalStateException("Cannot delete device without id"))
            )
        }

    fun upsertByExternalId(fromEvent: Device): Mono<Device> =
        repo.findByExternalId(fromEvent.externalId)
            .flatMap { existing ->
                val updated = existing.copy(
                    name = fromEvent.name,
                    brand = fromEvent.brand,
                    state = fromEvent.state
                    // externalId & creationTime preserved
                )
                repo.save(updated)
            }
            .switchIfEmpty(repo.save(fromEvent))

    class NotFoundException(message: String) : RuntimeException(message)
}
