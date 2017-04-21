package org.synyx.urlaubsverwaltung.config;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

import com.mangofactory.swagger.models.dto.ApiInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Configuration
@EnableSwagger
public class SwaggerConfig {

    @Value(value = "${info.app.version}")
    private String version;

    private SpringSwaggerConfig springSwaggerConfig;

    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {

        this.springSwaggerConfig = springSwaggerConfig;
    }


    @Bean
    public SwaggerSpringMvcPlugin swaggerSpringMvcPlugin() {

        SwaggerSpringMvcPlugin swaggerSpringMvcPlugin = new SwaggerSpringMvcPlugin(this.springSwaggerConfig);
        swaggerSpringMvcPlugin.apiVersion(version);
        swaggerSpringMvcPlugin.apiInfo(new ProjectApiInfo());
        swaggerSpringMvcPlugin.includePatterns("/api/.+");

        return swaggerSpringMvcPlugin;
    }

    private final class ProjectApiInfo extends ApiInfo {

        private ProjectApiInfo() {

            super("Urlaubsverwaltung API: " + version,
                "This Rest API provides the possibility to fetch information about the persons "
                + "and their vacation and sick notes.", null, "murygina@synyx.de", null, null);
        }
    }
}
