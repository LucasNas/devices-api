package com.lucas.devicesapikotlinreactive.infrastructure.messaging

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kafka")
data class KafkaProperties(
    var enabled: Boolean = false,
    var bootstrapServers: String = "",
    var topic: Topic = Topic()
) {
    data class Topic(
        var devices: String = "devices.events"
    )
}
