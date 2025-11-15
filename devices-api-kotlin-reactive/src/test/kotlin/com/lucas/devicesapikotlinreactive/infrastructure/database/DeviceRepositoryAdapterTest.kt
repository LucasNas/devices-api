package com.lucas.devicesapikotlinreactive.infrastructure.database

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.infrastructure.database.entity.DeviceEntity
import com.lucas.devicesapikotlinreactive.infrastructure.database.repository.DeviceR2dbcRepository
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
import java.util.UUID

class DeviceRepositoryAdapterTest {

    private val repo = mockk<DeviceR2dbcRepository>()
    private val adapter = DeviceRepositoryAdapter(repo)

    @AfterEach
    fun tearDown() = clearAllMocks()

    @Test
    @DisplayName("save() maps Device to DeviceEntity and back to Device")
    fun saveShouldMapToEntityAndBack() {
        val now = OffsetDateTime.now()
        val device = Device(
            id = null,
            externalId = UUID.randomUUID(),
            name = "iPhone 15",
            brand = "Apple",
            state = DeviceState.AVAILABLE,
            creationTime = now
        )

        val savedEntity = DeviceEntity(
            id = 1L,
            externalId = device.externalId,
            name = device.name,
            brand = device.brand,
            state = device.state,
            creationTime = device.creationTime
        )

        every { repo.save(any()) } returns Mono.just(savedEntity)

        val result = adapter.save(device)

        StepVerifier.create(result)
            .assertNext { saved ->
                assertThat(saved.id).isEqualTo(1L)
                assertThat(saved.externalId).isEqualTo(device.externalId)
                assertThat(saved.name).isEqualTo("iPhone 15")
                assertThat(saved.brand).isEqualTo("Apple")
                assertThat(saved.state).isEqualTo(DeviceState.AVAILABLE)
                assertThat(saved.creationTime).isEqualTo(now)
            }
            .verifyComplete()

        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    @DisplayName("findById() maps DeviceEntity to Device")
    fun findByIdShouldMapEntityToDomain() {
        val id = 10L
        val externalId = UUID.randomUUID()
        val creationTime = OffsetDateTime.now().minusDays(1)

        val entity = DeviceEntity(
            id = id,
            externalId = externalId,
            name = "Galaxy S24",
            brand = "Samsung",
            state = DeviceState.IN_USE,
            creationTime = creationTime
        )

        every { repo.findById(id) } returns Mono.just(entity)

        val result = adapter.findById(id)

        StepVerifier.create(result)
            .assertNext { device ->
                assertThat(device.id).isEqualTo(id)
                assertThat(device.externalId).isEqualTo(externalId)
                assertThat(device.name).isEqualTo("Galaxy S24")
                assertThat(device.brand).isEqualTo("Samsung")
                assertThat(device.state).isEqualTo(DeviceState.IN_USE)
                assertThat(device.creationTime).isEqualTo(creationTime)
            }
            .verifyComplete()

        verify(exactly = 1) { repo.findById(id) }
    }

    @Test
    @DisplayName("findAll() maps all DeviceEntity instances to domain Devices")
    fun findAllShouldMapAllEntities() {
        val entities = listOf(
            DeviceEntity(
                id = 1L,
                externalId = UUID.randomUUID(),
                name = "Pixel 9",
                brand = "Google",
                state = DeviceState.AVAILABLE,
                creationTime = OffsetDateTime.now().minusDays(2)
            ),
            DeviceEntity(
                id = 2L,
                externalId = UUID.randomUUID(),
                name = "Moto Edge",
                brand = "Motorola",
                state = DeviceState.RETIRED,
                creationTime = OffsetDateTime.now().minusDays(3)
            )
        )

        every { repo.findAll() } returns Flux.fromIterable(entities)

        val result = adapter.findAll()

        StepVerifier.create(result)
            .assertNext { d ->
                assertThat(d.id).isEqualTo(1L)
                assertThat(d.name).isEqualTo("Pixel 9")
            }
            .assertNext { d ->
                assertThat(d.id).isEqualTo(2L)
                assertThat(d.name).isEqualTo("Moto Edge")
            }
            .verifyComplete()

        verify(exactly = 1) { repo.findAll() }
    }

    @Test
    @DisplayName("findByBrand() delegates to repository and maps the result")
    fun findByBrandShouldDelegate() {
        val brand = "Apple"
        val entity = DeviceEntity(
            id = 1L,
            externalId = UUID.randomUUID(),
            name = "iPhone 15",
            brand = brand,
            state = DeviceState.AVAILABLE,
            creationTime = OffsetDateTime.now()
        )

        every { repo.findByBrand(brand) } returns Flux.just(entity)

        val result = adapter.findByBrand(brand)

        StepVerifier.create(result)
            .assertNext { d ->
                assertThat(d.brand).isEqualTo(brand)
                assertThat(d.name).isEqualTo("iPhone 15")
            }
            .verifyComplete()

        verify(exactly = 1) { repo.findByBrand(brand) }
    }

    @Test
    @DisplayName("delete() delegates to deleteById()")
    fun deleteShouldDelegateToDeleteById() {
        val id = 99L

        every { repo.deleteById(id) } returns Mono.empty()

        val result = adapter.delete(id)

        StepVerifier.create(result).verifyComplete()

        verify(exactly = 1) { repo.deleteById(id) }
    }

    @Test
    @DisplayName("findByExternalId() maps DeviceEntity to Device")
    fun findByExternalIdShouldMapEntityToDomain() {
        val externalId = UUID.randomUUID()
        val entity = DeviceEntity(
            id = 5L,
            externalId = externalId,
            name = "Busy Phone",
            brand = "Apple",
            state = DeviceState.IN_USE,
            creationTime = OffsetDateTime.now().minusHours(5)
        )

        every { repo.findByExternalId(externalId) } returns Mono.just(entity)

        val result = adapter.findByExternalId(externalId)

        StepVerifier.create(result)
            .assertNext { d ->
                assertThat(d.id).isEqualTo(5L)
                assertThat(d.externalId).isEqualTo(externalId)
                assertThat(d.name).isEqualTo("Busy Phone")
            }
            .verifyComplete()

        verify(exactly = 1) { repo.findByExternalId(externalId) }
    }
}
