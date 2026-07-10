package com.srmasset.creditengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI creditEngineOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SRM Credit Engine API")
                .description("Plataforma de cessao de credito multimoedas - FIDC")
                .version("v1"));
    }
}
