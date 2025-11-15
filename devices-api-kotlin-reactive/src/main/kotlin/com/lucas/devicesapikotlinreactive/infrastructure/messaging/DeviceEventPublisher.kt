package com.lucas.devicesapikotlinreactive.infrastructure.messaging

import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.port.DeviceEventPublisherPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
class DeviceEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, DeviceEvent>,
    private val kafkaProps: KafkaProperties
) : DeviceEventPublisherPort {

    override fun publishCreated(device: Device): Mono<Void> {
        val event = device.toEvent(origin = "KOTLIN")

        return Mono.fromRunnable {
            kafkaTemplate.send(
                kafkaProps.topic.devices,
                event.externalId.toString(),
                event
            )
        }
    }

}
