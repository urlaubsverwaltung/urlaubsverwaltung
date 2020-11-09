package org.synyx.urlaubsverwaltung;

import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static freemarker.template.Configuration.VERSION_2_3_30;

@Configuration
public class FreemarkerConfig extends FreeMarkerAutoConfiguration {

    private final freemarker.template.Configuration configuration;

    public FreemarkerConfig(ApplicationContext applicationContext, FreeMarkerProperties properties, freemarker.template.Configuration configuration) {
        super(applicationContext, properties);
        this.configuration = configuration;
    }

    @PostConstruct
    public void postConstruct() {
        configuration.setObjectWrapper(new Java8ObjectWrapper(VERSION_2_3_30));
    }
}
