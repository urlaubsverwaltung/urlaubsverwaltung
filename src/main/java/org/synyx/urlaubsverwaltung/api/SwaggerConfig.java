package org.synyx.urlaubsverwaltung.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

import static springfox.documentation.spi.DocumentationType.OAS_30;


@Configuration
public class SwaggerConfig {

    public static final String EXAMPLE_YEAR = "2019";
    public static final String EXAMPLE_LAST_DAY_OF_YEAR = EXAMPLE_YEAR + "-12-31";
    public static final String EXAMPLE_LAST_DAY_OF_MONTH = EXAMPLE_YEAR + "-01-31";
    public static final String EXAMPLE_FIRST_DAY_OF_YEAR = EXAMPLE_YEAR + "-01-01";

    @Value(value = "${info.app.version}")
    private String version;

    @Bean
    public Docket api() {
        return new Docket(OAS_30)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
            .build()
            .apiInfo(
                new ApiInfo(
                    "Urlaubsverwaltung API",
                    "This rest API provides the possibility to fetch information about absences, availabilities, persons, public holidays, sicknotes, vacations and many more...",
                    version,
                    null,
                    new Contact("Urlaubsverwaltung", "https://github.com/synyx/urlaubsverwaltung", "urlaubsverwaltung@synyx.de"),
                    "Apache 2.0",
                    "https://github.com/synyx/urlaubsverwaltung/blob/master/LICENSE.txt",
                    List.of())
            );
    }
}
