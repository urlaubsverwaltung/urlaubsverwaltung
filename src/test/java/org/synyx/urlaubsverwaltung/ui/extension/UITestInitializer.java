package org.synyx.urlaubsverwaltung.ui.extension;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

class UITestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addApplicationListener((ApplicationListener<WebServerInitializedEvent>) event ->
            org.testcontainers.Testcontainers.exposeHostPorts(event.getWebServer().getPort()));
    }
}
