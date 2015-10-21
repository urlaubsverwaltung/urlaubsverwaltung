package org.synyx.urlaubsverwaltung.security;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;


/**
 * Configuration for authentication via Active Directory.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Configuration
@ImportResource("classpath:/META-INF/spring-security-activeDirectory.xml")
@Conditional(ActiveDirectoryAuthenticationCondition.class)
public class ActiveDirectoryAuthenticationConfiguration {

    // OK
}
