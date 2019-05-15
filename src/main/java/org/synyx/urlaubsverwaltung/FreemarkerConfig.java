package org.synyx.urlaubsverwaltung;

import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class FreemarkerConfig extends FreeMarkerAutoConfiguration {

    @Autowired
    private freemarker.template.Configuration configuration;

    public FreemarkerConfig(ApplicationContext applicationContext, FreeMarkerProperties properties) {
        super(applicationContext, properties);
    }

    @PostConstruct
    public void postConstruct() {
        configuration.setObjectWrapper(
            new Java8ObjectWrapper(freemarker.template.Configuration.getVersion()));
    }

}
