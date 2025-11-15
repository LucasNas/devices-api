package com.lucas.devicesapikotlinreactive.infrastructure.api

import com.lucas.devicesapikotlinreactive.application.service.DeviceService
import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import com.lucas.devicesapikotlinreactive.infrastructure.api.dto.DeviceRequest
import com.lucas.devicesapikotlinreactive.infrastructure.api.dto.DeviceResponse
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class DeviceControllerTest {

    private val service = mockk<DeviceService>()
    private val controller = DeviceController(service)
    private val client: WebTestClient = WebTestClient.bindToController(controller)
        .controllerAdvice(RestExceptionHandler())
        .build()

    @AfterEach
    fun tearDown() = clearAllMocks()

    @Test
    @DisplayName("POST /api/devices creates a device and returns 201 with body")
    fun createShouldReturnCreatedDevice() {
        val request = DeviceRequest(
            name = "iPhone 15",
            brand = "Apple",
            state = DeviceState.AVAILABLE
        )

        val device = Device.create(
            name = request.name,
            brand = request.brand,
            state = request.state
        ).copy(id = 1L)

        every { service.create(request.name, request.brand, request.state) } returns Mono.just(device)

        val result = client.post()
            .uri("/api/devices")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody(DeviceResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.name).isEqualTo("iPhone 15")
        assertThat(result.brand).isEqualTo("Apple")
        assertThat(result.state).isEqualTo(DeviceState.AVAILABLE.name)

        verify(exactly = 1) {
            service.create(request.name, request.brand, request.state)
        }
    }

    @Test
    @DisplayName("GET /api/devices/{id} returns device when found")
    fun getShouldReturnDeviceWhenFound() {
        val id = 1L
        val device = Device.create("Galaxy S24", "Samsung", DeviceState.IN_USE)
            .copy(id = id)

        every { service.get(id) } returns Mono.just(device)

        val result = client.get()
            .uri("/api/devices/{id}", id)
            .exchange()
            .expectStatus().isOk
            .expectBody(DeviceResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo("Galaxy S24")
        assertThat(result.brand).isEqualTo("Samsung")
        assertThat(result.state).isEqualTo(DeviceState.IN_USE.name)

        verify(exactly = 1) { service.get(id) }
    }

    @Test
    @DisplayName("GET /api/devices/{id} returns 404 when service throws NotFoundException")
    fun getShouldReturn404WhenNotFound() {
        val id = 42L
        every { service.get(id) } returns Mono.error(
            DeviceService.NotFoundException("Device $id not found")
        )

        client.get()
            .uri("/api/devices/{id}", id)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.error").isEqualTo("NOT_FOUND")
            .jsonPath("$.message").isEqualTo("Device 42 not found")

        verify(exactly = 1) { service.get(id) }
    }

    @Test
    @DisplayName("GET /api/devices without filters returns all devices")
    fun listShouldReturnAllDevicesWhenNoFilters() {
        val devices = listOf(
            Device.create("Pixel 9", "Google", DeviceState.AVAILABLE).copy(id = 1L),
            Device.create("Moto Edge", "Motorola", DeviceState.RETIRED).copy(id = 2L)
        )

        every { service.all() } returns Flux.fromIterable(devices)

        val result = client.get()
            .uri("/api/devices")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(DeviceResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Pixel 9")
        assertThat(result[1].name).isEqualTo("Moto Edge")

        verify(exactly = 1) { service.all() }
    }

    @Test
    @DisplayName("GET /api/devices?brand= filters by brand")
    fun listShouldFilterByBrand() {
        val brand = "Apple"
        val devices = listOf(
            Device.create("iPhone 14", brand, DeviceState.AVAILABLE).copy(id = 1L),
            Device.create("iPhone 15", brand, DeviceState.IN_USE).copy(id = 2L)
        )

        every { service.byBrand(brand) } returns Flux.fromIterable(devices)

        val result = client.get()
            .uri { uriBuilder ->
                uriBuilder.path("/api/devices")
                    .queryParam("brand", brand)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBodyList(DeviceResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(result).hasSize(2)
        assertThat(result[0].brand).isEqualTo(brand)
        assertThat(result[1].brand).isEqualTo(brand)

        verify(exactly = 1) { service.byBrand(brand) }
    }

    @Test
    @DisplayName("GET /api/devices?state= filters by state")
    fun listShouldFilterByState() {
        val state = DeviceState.IN_USE
        val devices = listOf(
            Device.create("iPhone 13", "Apple", state).copy(id = 1L),
            Device.create("Galaxy S23", "Samsung", state).copy(id = 2L)
        )

        every { service.byState(state) } returns Flux.fromIterable(devices)

        val result = client.get()
            .uri { uriBuilder ->
                uriBuilder.path("/api/devices")
                    .queryParam("state", state)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBodyList(DeviceResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(result).hasSize(2)
        assertThat(result[0].state).isEqualTo(state.name)
        assertThat(result[1].state).isEqualTo(state.name)

        verify(exactly = 1) { service.byState(state) }
    }

    @Test
    @DisplayName("PUT /api/devices/{id} performs a full update and returns updated device")
    fun fullUpdateShouldReturnUpdatedDevice() {
        val id = 10L
        val request = DeviceRequest(
            name = "Updated Name",
            brand = "Updated Brand",
            state = DeviceState.AVAILABLE
        )

        val updatedDevice = Device.create(
            name = request.name,
            brand = request.brand,
            state = request.state
        ).copy(id = id)

        every { service.updateFull(id, any()) } returns Mono.just(updatedDevice)

        val result = client.put()
            .uri("/api/devices/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(DeviceResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo("Updated Name")
        assertThat(result.brand).isEqualTo("Updated Brand")
        assertThat(result.state).isEqualTo(DeviceState.AVAILABLE.name)

        verify(exactly = 1) { service.updateFull(id, any()) }
    }

    @Test
    @DisplayName("DELETE /api/devices/{id} returns 204 No Content")
    fun deleteShouldReturnNoContent() {
        val id = 99L
        every { service.delete(id) } returns Mono.empty()

        client.delete()
            .uri("/api/devices/{id}", id)
            .exchange()
            .expectStatus().isNoContent

        verify(exactly = 1) { service.delete(id) }
    }
}
