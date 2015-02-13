
package org.synyx.urlaubsverwaltung.core.startup;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;


/**
 * This service is executed every time the application is started to log information about the application configuration
 * like database user and url.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class StartupService {

    private static final Logger LOG = Logger.getLogger(StartupService.class);

    private static final String SPRING_PROFILE_ACTIVE = "spring.profiles.active";
    private static final List<String> POSSIBLE_PROFILES = Arrays.asList("ldap", "activeDirectory", "default");

    private final String dbUser;
    private final String dbUrl;
    private final String emailManager;
    private final String activeSpringProfile;

    @Autowired
    public StartupService(@Value("${db.username}") String dbUser,
        @Value("${db.url}") String dbUrl,
        @Value("${mail.manager}") String emailManager) {

        this.dbUser = dbUser;
        this.dbUrl = dbUrl;
        this.emailManager = emailManager;
        this.activeSpringProfile = System.getProperty(SPRING_PROFILE_ACTIVE);

        // Ensure that the given Spring profile is valid
        if (activeSpringProfile != null) {
            Optional<String> validProfile = Iterables.tryFind(POSSIBLE_PROFILES, new Predicate<String>() {

                        @Override
                        public boolean apply(String profile) {

                            return profile.equals(activeSpringProfile);
                        }
                    });

            if (!validProfile.isPresent()) {
                throw new RuntimeException("INVALID VALUE FOR '" + SPRING_PROFILE_ACTIVE + "'=" + activeSpringProfile);
            }
        }
    }

    @PostConstruct
    public void logStartupInfo() {

        LOG.info("DATABASE=" + dbUrl);
        LOG.info("DATABASE USER=" + dbUser);
        LOG.info("APPLICATION MANAGER EMAIL=" + emailManager);
        LOG.info("ACTIVE SPRING PROFILE=" + activeSpringProfile);
    }
}
