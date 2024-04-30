package ru.vsu.cs.sheina.online_gallery_backend.configuration;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Lindéro API",
                description = "The Lindéro project allows you to sell and buy paintings, sculptures and photographs. Sales are carried out through auction or at a fixed price.", version = "0.0.1",
                contact = @Contact(
                        name = "Elena Sheina",
                        email = "lena_sheina@bk.ru"
                )
        ),
        servers = {@Server(url = "http://localhost:8080", description = "Dev server")}
)
public class SwaggerConfig {
}
