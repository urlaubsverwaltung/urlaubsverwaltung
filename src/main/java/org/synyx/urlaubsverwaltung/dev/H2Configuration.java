package org.synyx.urlaubsverwaltung.dev;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;


/**
 * Start h2 web server for development purpose.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Configuration
@ImportResource("classpath:/META-INF/h2.xml")
@Conditional(DevEnvironmentCondition.class)
public class H2Configuration {

    // OK

}
