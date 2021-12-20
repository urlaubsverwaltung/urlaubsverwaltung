package org.synyx.urlaubsverwaltung.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.Constants;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springdoc.core.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value(value = "${info.app.version}")
    private String version;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Urlaubsverwaltung API")
                    .version(version)
                    .description("This rest API provides the possibility to fetch information about absences," +
                        " availabilities, persons, public holidays, sicknotes, vacations and many more...")
                    .contact(
                        new Contact()
                            .name("Urlaubsverwaltung")
                            .url("https://urlaubsverwaltung.cloud")
                            .email("info@urlaubsverwaltung.cloud")
                    )
                    .license(
                        new License()
                            .name("Apache 2.0")
                            .url("https://github.com/synyx/urlaubsverwaltung/blob/main/LICENSE.txt")
                    )
            );

    }

    @Bean
    public SwaggerIndexTransformer indexPageTransformer(SpringDocConfigProperties springDocConfigProperties, SwaggerUiConfigProperties swaggerUiConfig, SwaggerUiOAuthProperties swaggerUiOAuthProperties, SwaggerUiConfigParameters swaggerUiConfigParameters, ObjectMapper objectMapper, SwaggerWelcomeCommon swaggerWelcomeCommon) {
        return new SwaggerIndexPageTransformer(swaggerUiConfig, swaggerUiOAuthProperties, swaggerUiConfigParameters, objectMapper, swaggerWelcomeCommon) {
            @Override
            protected String overwriteSwaggerDefaultUrl(String html) {
                return html.replace(Constants.SWAGGER_UI_DEFAULT_URL, springDocConfigProperties.getApiDocs().getPath());
            }
        };
    }
}
