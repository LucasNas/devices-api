package com.lucas.devicesapikotlinreactive.configuration

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Devices API – Kotlin Reactive",
        version = "1.0",
        description = "Reactive REST API for managing devices using Spring WebFlux."
    ),
    servers = [
        Server(
            url = "http://localhost:8081",
            description = "Local Kotlin environment"
        )
    ]
)
class OpenApiConfig
