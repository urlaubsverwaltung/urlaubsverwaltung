package org.synyx.urlaubsverwaltung.api.config;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;


@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value(value = "${info.app.version}")
    private String version;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
            .build()
            .apiInfo(apiInfo());
    }


    private ApiInfo apiInfo() {
        return new ApiInfo(
            "Urlaubsverwaltung API",
            "This rest API provides the possibility to fetch information about " +
                "absences, availabilities, persons, public holidays, sicknotes, vacations and working days",
            version,
            "Terms of service",
            new Contact("synyx GmbH", "https://github.com/synyx/urlaubsverwaltung", "urlaubsverwaltung@synyx.de"),
            "Apache 2.0", "https://github.com/synyx/urlaubsverwaltung/blob/master/LICENSE.txt", Collections.emptyList());
    }
}
