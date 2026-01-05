package fr.cnam.reportui.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI/Swagger.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reportUiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ReportUI API")
                .description("API REST pour la gestion des rapports de migration ant2maven")
                .version("1.0.0")
                .contact(new Contact()
                    .name("CNAM")
                    .email("support@cnam.fr")));
    }
}
