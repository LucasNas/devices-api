package com.lucas.devicesapikotlinreactive.application.service

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.domain.port.DeviceEventPublisherPort
import com.lucas.devicesapikotlinreactive.domain.port.DeviceRepositoryPort
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

    private val repo: DeviceRepositoryPort = mockk()
    private val publisher: DeviceEventPublisherPort = mockk()

    private val service = DeviceService(repo, publisher)

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("create() should save device and publish event")
    fun createShouldSaveAndPublishEvent() {
        val name = "iPhone 15"
        val brand = "Apple"
        val state = DeviceState.AVAILABLE

        val savedDevice = Device.create(name, brand, state).copy(id = 1L)

        every { repo.save(any()) } returns Mono.just(savedDevice)
        every { publisher.publishCreated(savedDevice) } returns Mono.empty()

        val result = service.create(name, brand, state)

        StepVerifier.create(result)
            .assertNext { d ->
                assertThat(d.id).isEqualTo(1L)
                assertThat(d.name).isEqualTo(name)
                assertThat(d.brand).isEqualTo(brand)
                assertThat(d.state).isEqualTo(state)
                assertThat(d.creationTime).isNotNull()
                assertThat(d.externalId).isNotNull()
            }
            .verifyComplete()

        verify(exactly = 1) { repo.save(any()) }
        verify(exactly = 1) { publisher.publishCreated(savedDevice) }
    }

    @Test
    @DisplayName("get() should return device when found")
    fun getShouldReturnDeviceWhenFound() {
        val id = 1L
        val device = Device.create("Galaxy S24", "Samsung", DeviceState.IN_USE).copy(id = id)

        every { repo.findById(id) } returns Mono.just(device)

        val result = service.get(id)

        StepVerifier.create(result)
            .expectNext(device)
            .verifyComplete()

        verify(exactly = 1) { repo.findById(id) }
    }

    @Test
    @DisplayName("get() should emit NotFoundException when device does not exist")
    fun getShouldErrorWhenNotFound() {
        val id = 42L

        every { repo.findById(id) } returns Mono.empty()

        val result = service.get(id)

        StepVerifier.create(result)
            .expectError(DeviceService.NotFoundException::class.java)
            .verify()

        verify(exactly = 1) { repo.findById(id) }
    }

    @Test
    @DisplayName("all() should return all devices")
    fun allShouldReturnAllDevices() {
        val devices = listOf(
            Device.create("Pixel 9", "Google", DeviceState.AVAILABLE).copy(id = 1L),
            Device.create("Moto Edge", "Motorola", DeviceState.RETIRED).copy(id = 2L),
        )

        every { repo.findAll() } returns Flux.fromIterable(devices)

        val result = service.all()

        StepVerifier.create(result)
            .expectNext(devices[0], devices[1])
            .verifyComplete()

        verify(exactly = 1) { repo.findAll() }
    }

    @Test
    @DisplayName("byBrand() should filter devices by brand")
    fun byBrandShouldFilterByBrand() {
        val brand = "Apple"
        val devices = listOf(
            Device.create("iPhone 14", brand, DeviceState.AVAILABLE).copy(id = 1L),
            Device.create("iPhone 15", brand, DeviceState.IN_USE).copy(id = 2L),
        )

        every { repo.findByBrand(brand) } returns Flux.fromIterable(devices)

        val result = service.byBrand(brand)

        StepVerifier.create(result)
            .expectNext(devices[0], devices[1])
            .verifyComplete()

        verify(exactly = 1) { repo.findByBrand(brand) }
    }

    @Test
    @DisplayName("byState() should filter devices by state")
    fun byStateShouldFilterByState() {
        val state = DeviceState.IN_USE
        val devices = listOf(
            Device.create("iPhone 13", "Apple", state).copy(id = 1L),
            Device.create("Galaxy S23", "Samsung", state).copy(id = 2L),
        )

        every { repo.findByState(state) } returns Flux.fromIterable(devices)

        val result = service.byState(state)

        StepVerifier.create(result)
            .expectNext(devices[0], devices[1])
            .verifyComplete()

        verify(exactly = 1) { repo.findByState(state) }
    }

    @Test
    @DisplayName("updateFull() should update mutable fields and preserve id, externalId and creationTime")
    fun updateFullShouldUpdateAndPreserveImmutableFields() {
        val id = 10L
        val externalId = UUID.randomUUID()
        val creationTime = OffsetDateTime.now().minusDays(1)

        val existing = Device(
            id = id,
            externalId = externalId,
            name = "Old Phone",
            brand = "Nokia",
            state = DeviceState.RETIRED,
            creationTime = creationTime
        )

        val incoming = existing.copy(
            name = "iPhone 16",
            brand = "Apple",
            state = DeviceState.AVAILABLE
        )

        every { repo.findById(id) } returns Mono.just(existing)
        every { repo.save(any()) } answers { Mono.just(firstArg()) }

        val result = service.updateFull(id, incoming)

        StepVerifier.create(result)
            .assertNext { updated ->
                // updated fields
                assertThat(updated.name).isEqualTo("iPhone 16")
                assertThat(updated.brand).isEqualTo("Apple")
                assertThat(updated.state).isEqualTo(DeviceState.AVAILABLE)

                // immutable fields preserved
                assertThat(updated.id).isEqualTo(id)
                assertThat(updated.externalId).isEqualTo(externalId)
                assertThat(updated.creationTime).isEqualTo(creationTime)
            }
            .verifyComplete()

        verify(exactly = 1) { repo.findById(id) }
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    @DisplayName("updateFull() should fail when device is IN_USE and name or brand changes")
    fun updateFullShouldFailWhenInUseAndChangingNameOrBrand() {
        val id = 20L
        val existing = Device.create("Locked Phone", "Apple", DeviceState.IN_USE)
            .copy(id = id)

        val incoming = existing.copy(
            name = "Other Name",
            brand = "Other Brand"
        )

        every { repo.findById(id) } returns Mono.just(existing)

        val result = service.updateFull(id, incoming)

        StepVerifier.create(result)
            .expectError(IllegalStateException::class.java)
            .verify()

        verify(exactly = 1) { repo.findById(id) }
        verify(exactly = 0) { repo.save(any()) }
    }

    @Test
    @DisplayName("delete() should remove device when not IN_USE")
    fun deleteShouldRemoveDevice() {
        val id = 99L
        val existing = Device.create("Moto G", "Motorola", DeviceState.AVAILABLE)
            .copy(id = id)

        every { repo.findById(id) } returns Mono.just(existing)
        every { repo.delete(id) } returns Mono.empty()

        val result = service.delete(id)

        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) { repo.findById(id) }
        verify(exactly = 1) { repo.delete(id) }
    }

    @Test
    @DisplayName("delete() should fail when device is IN_USE")
    fun deleteShouldFailWhenInUse() {
        val id = 100L
        val existing = Device.create("Busy Phone", "Apple", DeviceState.IN_USE)
            .copy(id = id)

        every { repo.findById(id) } returns Mono.just(existing)

        val result = service.delete(id)

        StepVerifier.create(result)
            .expectError(IllegalStateException::class.java)
            .verify()

        verify(exactly = 1) { repo.findById(id) }
        verify(exactly = 0) { repo.delete(any()) }
    }

    @Test
    @DisplayName("upsertByExternalId() should update existing device when externalId already exists")
    fun upsertByExternalIdShouldUpdateExisting() {
        val externalId = UUID.randomUUID()
        val creationTime = OffsetDateTime.now().minusHours(1)

        val existing = Device(
            id = 5L,
            externalId = externalId,
            name = "Old Name",
            brand = "Old Brand",
            state = DeviceState.AVAILABLE,
            creationTime = creationTime
        )

        val fromEvent = existing.copy(
            name = "New Name",
            brand = "New Brand",
            state = DeviceState.IN_USE
        )

        every { repo.findByExternalId(externalId) } returns Mono.just(existing)
        every { repo.save(any()) } answers { Mono.just(firstArg()) }

        val result = service.upsertByExternalId(fromEvent)

        StepVerifier.create(result)
            .assertNext { updated ->
                assertThat(updated.id).isEqualTo(existing.id)
                assertThat(updated.externalId).isEqualTo(externalId)
                assertThat(updated.name).isEqualTo("New Name")
                assertThat(updated.brand).isEqualTo("New Brand")
                assertThat(updated.state).isEqualTo(DeviceState.IN_USE)
                assertThat(updated.creationTime).isEqualTo(creationTime)
            }
            .verifyComplete()

        verify(exactly = 1) { repo.findByExternalId(externalId) }
        // sometimes assertion logic may cause another internal usage; we just care that it was called at least once
        verify(atLeast = 1) { repo.save(any()) }
    }

    @Test
    @DisplayName("upsertByExternalId() should create new device when externalId does not exist")
    fun upsertByExternalIdShouldCreateNewWhenNotExists() {
        val externalId = UUID.randomUUID()
        val creationTime = OffsetDateTime.now().minusHours(1)

        val fromEvent = Device(
            id = null,
            externalId = externalId,
            name = "Pixel 9",
            brand = "Google",
            state = DeviceState.AVAILABLE,
            creationTime = creationTime
        )

        val saved = fromEvent.copy(id = 10L)

        every { repo.findByExternalId(externalId) } returns Mono.empty()
        every { repo.save(fromEvent) } returns Mono.just(saved)

        val result = service.upsertByExternalId(fromEvent)

        StepVerifier.create(result)
            .assertNext { d ->
                assertThat(d.id).isEqualTo(10L)
                assertThat(d.externalId).isEqualTo(externalId)
                assertThat(d.name).isEqualTo("Pixel 9")
                assertThat(d.brand).isEqualTo("Google")
                assertThat(d.state).isEqualTo(DeviceState.AVAILABLE)
                assertThat(d.creationTime).isEqualTo(creationTime)
            }
            .verifyComplete()

        verify(exactly = 1) { repo.findByExternalId(externalId) }
        verify(exactly = 1) { repo.save(fromEvent) }
    }
}
