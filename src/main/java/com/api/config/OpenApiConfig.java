package com.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Class OpenApiConfig
 *
 * Configuration class for setting up OpenAPI documentation for the Task Management System.
 * It provides API metadata such as the title, description, version, and contact information for the API.
 * Additionally, it configures the security scheme used by the API, which is based on JWT authentication.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Bank Cards Management System",
                description = "Test Api", version = "1.0.0",
                contact = @Contact(
                        name = "zhukovskaja.elena@gmail.com",
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