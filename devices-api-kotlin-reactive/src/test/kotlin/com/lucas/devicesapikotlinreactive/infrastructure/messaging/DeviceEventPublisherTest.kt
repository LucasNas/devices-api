package com.lucas.devicesapikotlinreactive.infrastructure.messaging

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import java.util.*

class DeviceEventPublisherTest {

    private val kafkaTemplate = mockk<KafkaTemplate<String, DeviceEvent>>()
    private val kafkaProps = KafkaProperties(
        enabled = true,
        bootstrapServers = "localhost:9092",
        topic = KafkaProperties.Topic(devices = "devices.events.test")
    )

    private val publisher = DeviceEventPublisher(kafkaTemplate, kafkaProps)

    @AfterEach
    fun tearDown() = clearAllMocks()

    @Test
    @DisplayName("publishCreated() sends a DeviceEvent to the configured Kafka topic")
    fun publishCreatedShouldSendEventToKafka() {
        val device = Device(
            id = 1L,
            externalId = UUID.randomUUID(),
            name = "iPhone 15",
            brand = "Apple",
            state = DeviceState.AVAILABLE,
            creationTime = OffsetDateTime.now()
        )

        val topicSlot = slot<String>()
        val keySlot = slot<String>()
        val eventSlot = slot<DeviceEvent>()

        every {
            kafkaTemplate.send(capture(topicSlot), capture(keySlot), capture(eventSlot))
        } returns mockk(relaxed = true)

        val result = publisher.publishCreated(device)

        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) {
            kafkaTemplate.send(any(), any(), any())
        }

        assertThat(topicSlot.captured).isEqualTo("devices.events.test")
        assertThat(keySlot.captured).isEqualTo(device.externalId.toString())

        val event = eventSlot.captured
        assertThat(event.externalId).isEqualTo(device.externalId)
        assertThat(event.name).isEqualTo(device.name)
        assertThat(event.brand).isEqualTo(device.brand)
        assertThat(event.state).isEqualTo(device.state.name)
        assertThat(event.creationTime).isEqualTo(device.creationTime)
        assertThat(event.origin).isEqualTo("KOTLIN")
    }
}
