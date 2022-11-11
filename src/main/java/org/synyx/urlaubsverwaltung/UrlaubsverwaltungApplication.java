package org.synyx.urlaubsverwaltung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class UrlaubsverwaltungApplication extends SpringBootServletInitializer {

    @Override
    @Deprecated
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(UrlaubsverwaltungApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(UrlaubsverwaltungApplication.class, args);
    }
}
