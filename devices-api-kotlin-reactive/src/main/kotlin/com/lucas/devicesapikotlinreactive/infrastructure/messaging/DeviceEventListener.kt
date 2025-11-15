package com.lucas.devicesapikotlinreactive.infrastructure.messaging

import com.lucas.devicesapikotlinreactive.application.service.DeviceService
import com.lucas.devicesapikotlinreactive.domain.model.Device
import com.lucas.devicesapikotlinreactive.domain.model.DeviceState
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@ConditionalOnProperty(value = ["kafka.enabled"], havingValue = "true")
class DeviceEventListener(
    private val service: DeviceService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${kafka.topic.devices}"],
        groupId = "kotlin-devices",
        containerFactory = "deviceEventKafkaListenerFactory"
    )
    fun onDeviceEvent(event: DeviceEvent) {
        try {
            if (event.origin == "KOTLIN") return

            val device = Device.fromEvent(
                externalId = event.externalId,
                name = event.name,
                brand = event.brand,
                state = DeviceState.valueOf(event.state),
                creationTime = event.creationTime
            )


            service.upsertByExternalId(device)
                .onErrorResume {
                    log.error("Failed to upsert device from event: $event", it)
                    Mono.empty()
                }
                .subscribe()

        } catch (ex: Exception) {
            log.error("Failed to process DeviceEvent in Kotlin: $event", ex)
        }
    }
}
