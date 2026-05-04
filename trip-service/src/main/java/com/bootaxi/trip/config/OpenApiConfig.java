package com.bootaxi.trip.config;

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
    OpenAPI tripServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bo Taxi Trip Service API")
                        .version("1.0.0")
                        .description("Trip creation, atomic driver assignment, status changes, ratings, history, and daily statistics."))
                .addServersItem(new Server().url("http://192.168.0.138:8082").description("LAN Trip Service"))
                .addServersItem(new Server().url("http://localhost:8082").description("Localhost Trip Service"))
                .addServersItem(new Server().url("/").description("Current Trip Service origin"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
