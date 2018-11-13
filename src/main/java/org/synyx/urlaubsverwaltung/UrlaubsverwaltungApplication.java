package org.synyx.urlaubsverwaltung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


/**
 * Spring Boot Entry Point.
 *
 * @author  David Schilling - schilling@synyx.de
 */
@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
public class UrlaubsverwaltungApplication extends SpringBootServletInitializer { // NOSONAR - no private constructor needed

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(UrlaubsverwaltungApplication.class);
    }

    /**
     * Start the Urlaubsverwaltung Spring Boot application.
     *
     * @param  args  arguments
     */
    public static void main(String[] args) { // NOSONAR - yes, this main method really should be uncommented!

        SpringApplication.run(UrlaubsverwaltungApplication.class, args);
    }
}
