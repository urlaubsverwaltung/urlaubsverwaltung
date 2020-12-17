package org.synyx.urlaubsverwaltung.security.development;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.annotation.PostConstruct;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnProperty(name = "uv.security.auth", havingValue = "development")
class LocalDevelopmentAuthConfiguration {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    @Bean
    AuthenticationProvider defaultAuthenticationProvider(PersonService personService) {
        return new LocalDevelopmentAuthenticationProvider(personService);
    }

    @PostConstruct
    void onlyUseForLocalDevelopment(){
        LOG.info("#################################################################");
        LOG.info("##         You are using `uv.security.auth=development`        ##");
        LOG.info("##     Do not use this authentication method in production     ##");
        LOG.info("##       This should only be used in local development         ##");
        LOG.info("#################################################################");
    }
}
