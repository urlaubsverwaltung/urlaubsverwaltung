
package org.synyx.urlaubsverwaltung.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;


/**
 * This service is executed every time the application is started to log information about the application configuration
 * like database user and url.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class StartupService {

    private static final Logger LOG = LoggerFactory.getLogger(StartupService.class);

    private final String dbUser;
    private final String dbUrl;
    private final String authentication;
    private final String[] activeProfiles;

    @Autowired
    public StartupService(@Value("${spring.datasource.username}") String dbUser,
        @Value("${spring.datasource.url}") String dbUrl,
        @Value("${auth}") String authentication, org.springframework.core.env.Environment env) {

        this.dbUser = dbUser;
        this.dbUrl = dbUrl;
        this.authentication = authentication;
        this.activeProfiles = env.getActiveProfiles();
    }

    @PostConstruct
    public void logStartupInfo() {

        LOG.info("DATABASE={}", dbUrl);
        LOG.info("DATABASE USER={}", dbUser);
        LOG.info("AUTHENTICATION={}", authentication);
        LOG.info("ACTIVE PROFILES={}", Arrays.toString(activeProfiles));
    }
}
