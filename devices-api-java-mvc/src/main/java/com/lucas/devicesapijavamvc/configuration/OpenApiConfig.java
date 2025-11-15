package com.lucas.devicesapijavamvc.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Devices API – Java MVC",
                version = "1.0",
                description = "REST API for managing electronic devices using Spring MVC."
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local Java environment"
                )
        }
)
public class OpenApiConfig {
}
