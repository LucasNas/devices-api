package com.lucas.devicesapikotlinreactive.domain.port

import com.lucas.devicesapikotlinreactive.domain.model.Device
import reactor.core.publisher.Mono

interface DeviceEventPublisherPort {
    fun publishCreated(device: Device): Mono<Void>
}
