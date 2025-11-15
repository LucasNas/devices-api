package com.lucas.devicesapikotlinreactive

import com.lucas.devicesapikotlinreactive.infrastructure.messaging.KafkaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties::class)
class DevicesApiKotlinReactiveApplication

fun main(args: Array<String>) {
    runApplication<DevicesApiKotlinReactiveApplication>(*args)
}
