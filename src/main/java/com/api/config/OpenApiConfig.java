package com.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Class OpenApiConfig
 *
 * Configuration class for OpenAPI documentation.
 *
 * Defines basic API metadata such as title, description, version, and contact info.
 * Also configures JWT bearer authentication for secured endpoints.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Bank Cards Management System",
                description = "Test Api", version = "1.0.0",
                contact = @Contact(
                        name = "Alena - zhukovskaja.elena@gmail.com",
                        email = "zhukovskaja.elena@gmail.com"
                )
        )
)
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {
}