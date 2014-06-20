package org.synyx.urlaubsverwaltung.restapi;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

import com.wordnik.swagger.model.ApiInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Configuration
@EnableSwagger
@ComponentScan(basePackages = { "org.synyx.urlaubsverwaltung.restapi" })
public class MySwaggerConfig {

    @Value(value = "${application.version}")
    private String version;

    private SpringSwaggerConfig springSwaggerConfig;

    /**
     * Required to autowire SpringSwaggerConfig.
     */
    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {

        this.springSwaggerConfig = springSwaggerConfig;
    }


    /**
     * Every SwaggerSpringMvcPlugin bean is picked up by the swagger-mvc framework - allowing for multiple swagger
     * groups i.e. same code base multiple swagger resource listings.
     */
    @Bean
    public SwaggerSpringMvcPlugin customImplementation() {

        SwaggerSpringMvcPlugin swaggerSpringMvcPlugin = new SwaggerSpringMvcPlugin(this.springSwaggerConfig);

        swaggerSpringMvcPlugin.apiVersion(version);
        swaggerSpringMvcPlugin.apiInfo(new ProjectApiInfo());

        return swaggerSpringMvcPlugin;
    }

    private class ProjectApiInfo extends ApiInfo {

        public ProjectApiInfo() {

            super("Urlaubsverwaltung API",
                "This Rest API provides the possibility to fetch information about the persons and their vacation and sick notes of the application.",
                null, "murygina@synyx.de", null, null);
        }
    }
}
