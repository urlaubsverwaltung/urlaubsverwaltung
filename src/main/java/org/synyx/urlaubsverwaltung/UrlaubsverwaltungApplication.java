package org.synyx.urlaubsverwaltung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class UrlaubsverwaltungApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlaubsverwaltungApplication.class, args);
    }
}
