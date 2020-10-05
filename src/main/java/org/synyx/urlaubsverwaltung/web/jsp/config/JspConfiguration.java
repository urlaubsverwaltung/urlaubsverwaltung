package org.synyx.urlaubsverwaltung.web.jsp.config;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnProperty(value = "uv.template-engine.jsp.use-precompiled", havingValue = "true")
@EnableConfigurationProperties(JspProperties.class)
public class JspConfiguration {

    private static final Logger LOG = getLogger(lookup().lookupClass());
    private static final String WEB_XML = "/WEB-INF/web.xml";

    @Bean
    public ServletContextInitializer registerPreCompiledJsps() {
        LOG.info("Application will use precompiled JSPs!");
        return new JspServletRegistrator(WEB_XML);
    }

}
