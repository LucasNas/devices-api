package com.lucas.devicesapikotlinreactive.application.service

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.domain.port.DeviceEventPublisherPort
import com.lucas.devicesapikotlinreactive.domain.port.DeviceRepositoryPort
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import java.util.*

class DeviceServiceTest {

    private val repo = mockk<DeviceRepositoryPort>()
    private val publisher = mockk<DeviceEventPublisherPort>()

    private val service = DeviceService(repo, publisher)

    @AfterEach
    fun tearDown() = clearAllMocks()

    @Test
    @DisplayName("create() saves a new device and publishes an event")
    fun createShouldSaveAndPublishEvent() {
        val name = "iPhone 15"
        val brand = "Apple"
        val state = DeviceState.AVAILABLE

        every { repo.save(any()) } answers {
            val d = firstArg<Device>().copy(id = 1L)
            Mono.just(d)
        }

        every { publisher.publishCreated(any()) } returns Mono.empty()

        val result = service.create(name, brand, state)

        StepVerifier.create(result)
            .assertNext { saved ->
                assertThat(saved.id).isEqualTo(1L)
                assertThat(saved.name).isEqualTo(name)
                assertThat(saved.brand).isEqualTo(brand)
                assertThat(saved.state).isEqualTo(state)
            }
            .verifyComplete()

        verify(exactly = 1) { repo.save(any()) }
        verify(exactly = 1) { publisher.publishCreated(any()) }
    }

    @Test
    @DisplayName("get() returns Device when found")
    fun getShouldReturnDeviceWhenFound() {
        val id = 1L
        val device = Device.create("Galaxy S24", "Samsung", DeviceState.IN_USE).copy(id = id)

        every { repo.findById(id) } returns Mono.just(device)

        val result = service.get(id)

        StepVerifier.create(result)
            .expectNext(device)
            .verifyComplete()

        verify { repo.findById(id) }
    }

    @Test
    @DisplayName("get() throws when missing")
    fun getShouldErrorWhenNotFound() {
        val id = 42L

        every { repo.findById(id) } returns Mono.empty()

        StepVerifier.create(service.get(id))
            .expectError(DeviceService.NotFoundException::class.java)
            .verify()

        verify { repo.findById(id) }
    }

    @Test
    @DisplayName("all() returns all devices")
    fun allShouldReturnAll() {
        val devices = listOf(
            Device.create("Pixel 9", "Google", DeviceState.AVAILABLE).copy(id = 1L),
            Device.create("Moto Edge", "Motorola", DeviceState.RETIRED).copy(id = 2L)
        )

        every { repo.findAll() } returns Flux.fromIterable(devices)

        StepVerifier.create(service.all())
            .expectNext(devices[0], devices[1])
            .verifyComplete()

        verify { repo.findAll() }
    }

    @Test
    @DisplayName("updateFull updates fields")
    fun updateFullShouldUpdate() {
        val id = 10L
        val existing = Device.create("Old Phone", "Nokia", DeviceState.RETIRED)
            .copy(id = id, externalId = UUID.randomUUID())

        val incoming = existing.copy(name = "iPhone 16", brand = "Apple", state = DeviceState.AVAILABLE)

        every { repo.findById(id) } returns Mono.just(existing)
        every { repo.save(any()) } answers { Mono.just(firstArg()) }

        val result = service.updateFull(id, incoming)

        StepVerifier.create(result)
            .assertNext { updated ->
                assertThat(updated.name).isEqualTo("iPhone 16")
                assertThat(updated.brand).isEqualTo("Apple")
                assertThat(updated.state).isEqualTo(DeviceState.AVAILABLE)

                assertThat(updated.id).isEqualTo(existing.id)
                assertThat(updated.externalId).isEqualTo(existing.externalId)
                assertThat(updated.creationTime).isEqualTo(existing.creationTime)
            }
            .verifyComplete()

        verify { repo.findById(id) }
        verify { repo.save(any()) }
    }

    @Test
    @DisplayName("delete() calls repo")
    fun deleteShouldCallRepo() {
        val id = 99L

        every { repo.delete(id) } returns Mono.empty()

        StepVerifier.create(service.delete(id))
            .verifyComplete()

        verify { repo.delete(id) }
    }

    @Test
    @DisplayName("upsert updates when exists")
    fun upsertShouldUpdate() {
        val externalId = UUID.randomUUID()
        val creationTime = OffsetDateTime.now().minusDays(1)

        val existing = Device.fromEvent(
            externalId, "Old", "OldBrand", DeviceState.RETIRED, creationTime
        ).copy(id = 5L)

        val incoming = Device.fromEvent(
            externalId, "New", "NewBrand", DeviceState.AVAILABLE, creationTime
        )

        every { repo.findByExternalId(externalId) } returns Mono.just(existing)
        every { repo.save(any()) } answers { Mono.just(firstArg()) }

        val result = service.upsertByExternalId(incoming)

        StepVerifier.create(result)
            .assertNext { updated ->
                assertThat(updated.name).isEqualTo("New")
                assertThat(updated.brand).isEqualTo("NewBrand")
                assertThat(updated.state).isEqualTo(DeviceState.AVAILABLE)
                assertThat(updated.id).isEqualTo(existing.id)
                assertThat(updated.creationTime).isEqualTo(creationTime)
            }
            .verifyComplete()

        verify { repo.findByExternalId(externalId) }
        verify { repo.save(any()) }
    }
}
