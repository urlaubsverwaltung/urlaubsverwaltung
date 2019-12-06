package org.synyx.urlaubsverwaltung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Spring Boot Entry Point.
 */
@SpringBootApplication
@EnableScheduling
public class UrlaubsverwaltungApplication { // NOSONAR - no private constructor needed

    /**
     * Start the Urlaubsverwaltung Spring Boot application.
     *
     * @param args arguments
     */
    public static void main(String[] args) { // NOSONAR - yes, this main method really should be uncommented!

        SpringApplication.run(UrlaubsverwaltungApplication.class, args);
    }
}
