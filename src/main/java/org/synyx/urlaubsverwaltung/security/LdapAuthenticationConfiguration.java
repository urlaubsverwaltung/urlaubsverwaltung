package org.synyx.urlaubsverwaltung.security;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;


/**
 * Configuration for authentication via LDAP.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Configuration
@ImportResource("classpath:/META-INF/spring-security-ldap.xml")
@Conditional(LdapAuthenticationCondition.class)
public class LdapAuthenticationConfiguration {

    // OK
}
