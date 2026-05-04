package com.bootaxi.user.config;

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
    OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bo Taxi User Service API")
                        .version("1.0.0")
                        .description("Passenger and driver registration, profiles, statuses, and JWT token issuing."))
                .addServersItem(new Server().url("http://192.168.0.138:8081").description("LAN User Service"))
                .addServersItem(new Server().url("http://localhost:8081").description("Localhost User Service"))
                .addServersItem(new Server().url("/").description("Current User Service origin"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
