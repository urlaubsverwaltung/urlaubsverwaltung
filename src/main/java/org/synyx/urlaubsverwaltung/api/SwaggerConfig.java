package org.synyx.urlaubsverwaltung.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Value(value = "${info.app.version}")
    private String version;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Urlaubsverwaltung API")
                    .version(version)
                    .description(
                        """
                            This API provides the functionality to

                            * fetch absences, vacations, sick notes, public holidays, persons, and many more...
                            * create persons
                            """
                    )
                    .contact(
                        new Contact()
                            .name("Urlaubsverwaltung")
                            .url("https://urlaubsverwaltung.cloud")
                            .email("info@urlaubsverwaltung.cloud")
                    )
                    .license(
                        new License()
                            .name("Apache 2.0")
                            .url("https://github.com/urlaubsverwaltung/urlaubsverwaltung/blob/main/LICENSE.txt")
                    )
            )
            .addSecurityItem(new SecurityRequirement()
                .addList(BEARER_AUTH)
            )
            .components(new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                    .name(BEARER_AUTH)
                    .type(HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"))
            );

    }
}
