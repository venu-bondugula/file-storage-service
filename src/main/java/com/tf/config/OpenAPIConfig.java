package com.tf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.log4j.Log4j2;

@Configuration
@SecurityScheme(name = "bearerAuth", description = "JWT token", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
@Log4j2
public class OpenAPIConfig {
    @Value("${instanceUrl:http://localhost:8080}")
    private String instanceUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("Picked up url {} for Swagger UI", instanceUrl);
        return new OpenAPI()
                .addServersItem(new Server().url(instanceUrl).description("Local Dev"))
                .info(new Info()
                        .version("1.0")
                        .contact(new Contact().email("venu1.bondugula@gmail.com").name("Venu Bondugula"))
                        .description("This is a simple file storage service API. " +
                                "You can upload, download, delete files and get the list of files uploaded by you. Enjoy the service!")
                        .title("File Storage Service"));
    }
}