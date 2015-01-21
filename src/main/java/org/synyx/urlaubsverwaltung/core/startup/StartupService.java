
package org.synyx.urlaubsverwaltung.core.startup;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
    private static final List<String> POSSIBLE_PROFILES = Arrays.asList("ldap", "activeDirectory", "default");

    @Value("${db.username}")
    private String dbUser;

    @Value("${db.url}")
    private String dbUrl;

    @Value("${mail.manager}")
    private String emailManager;

    @PostConstruct
    public void logStartupInfo() {

        LOG.info("DATABASE = " + dbUrl);
        LOG.info("DATABASE USER = " + dbUser);
        LOG.info("APPLICATION MANAGER EMAIL = " + emailManager);

        final String activeSpringProfile = System.getProperty(SPRING_PROFILE_ACTIVE);

        // Ensure that the given Spring profile is valid
        if (activeSpringProfile != null) {
            Optional<String> validProfile = Iterables.tryFind(POSSIBLE_PROFILES, new Predicate<String>() {

                        @Override
                        public boolean apply(String profile) {

                            return profile.equals(activeSpringProfile);
                        }
                    });

            if (!validProfile.isPresent()) {
                LOG.error("INVALID VALUE FOR '" + SPRING_PROFILE_ACTIVE + "' = " + activeSpringProfile);
                System.exit(1);
            }
        }

        LOG.info("ACTIVE SPRING PROFILE=" + activeSpringProfile);
    }
}
