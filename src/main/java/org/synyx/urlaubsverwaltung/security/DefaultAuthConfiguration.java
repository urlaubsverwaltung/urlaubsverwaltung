package org.synyx.urlaubsverwaltung.security;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;


/**
 * Configuration for default authentication, using credentials from database.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Configuration
@ImportResource("classpath:/META-INF/spring-security-default.xml")
@Conditional(DefaultAuthenticationCondition.class)
public class DefaultAuthConfiguration {

    // OK
}
