
package org.synyx.urlaubsverwaltung.core.startup;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


/**
 * This service is executed every time the application is started.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class StartupService {

    private static final Logger LOG = Logger.getLogger(StartupService.class);

    private static final String SPRING_PROFILE_ACTIVE = "spring.profiles.active";
    private static final String PROFILE_LDAP = "ldap";
    private static final String PROFILE_ACTIVE_DIRECTORY = "activeDirectory";
    private static final String PROFILE_DEFAULT = "default";

    @Value("${db.username}")
    private String dbUser;

    @Value("${db.url}")
    private String dbUrl;

    @Value("${email.manager}")
    private String emailManager;

    @PostConstruct
    public void logStartupInfo() {

        LOG.info("DATABASE = " + dbUrl);
        LOG.info("DATABASE USER = " + dbUser);
        LOG.info("APPLICATION MANAGER EMAIL = " + emailManager);

        // Ensure that the given Spring profile is valid
        String activeSpringProfile = System.getProperty(SPRING_PROFILE_ACTIVE);

        if (activeSpringProfile != null && !activeSpringProfile.equals(PROFILE_DEFAULT)
                && !activeSpringProfile.equals(PROFILE_LDAP) && !activeSpringProfile.equals(PROFILE_ACTIVE_DIRECTORY)) {
            LOG.error("INVALID VALUE FOR '" + SPRING_PROFILE_ACTIVE + "' = " + activeSpringProfile);
            System.exit(1);
        }

        LOG.info("ACTIVE SPRING PROFILE=" + activeSpringProfile);
    }
}
