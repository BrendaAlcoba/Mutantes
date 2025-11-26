package org.example.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Contact;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mutant Detector API")
                        .description("Examen Global")
                        .version("1.0.0")
                        .contact(new Contact() // Añadir información de contacto
                            .name("Magneto")
                            .url("https://github.com/BrendaAlcoba/Mutantes")
                            .email("brendaalcoba7676@gmail.com")));
    }

}
