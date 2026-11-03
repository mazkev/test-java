package com.belajar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI marketplaceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Marketplace Backend API")
                        .description("Dokumentasi Resmi REST API Marketplace Skala Besar")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Backend Engineering Team")
                                .email("engineering@marketplace.com")));
    }
}
