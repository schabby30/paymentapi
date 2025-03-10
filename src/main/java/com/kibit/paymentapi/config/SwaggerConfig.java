package com.kibit.paymentapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(info = @Info(title = "Instant Payment API", version = "0.0.1", description = "REST API handling instant payments"))
@Configuration
public class SwaggerConfig { }
