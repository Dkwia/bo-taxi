package com.bootaxi.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bo Taxi Notification Service API")
                        .version("1.0.0")
                        .description("Notification task queue API backed by concurrent worker processing."))
                .addServersItem(new Server().url("http://192.168.0.138:8083").description("LAN Notification Service"))
                .addServersItem(new Server().url("http://localhost:8083").description("Localhost Notification Service"))
                .addServersItem(new Server().url("/").description("Current Notification Service origin"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
