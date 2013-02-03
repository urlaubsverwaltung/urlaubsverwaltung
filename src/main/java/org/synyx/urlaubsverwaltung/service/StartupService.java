
package org.synyx.urlaubsverwaltung.service;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.synyx.urlaubsverwaltung.calendar.TestGoogleCalendarService;

/**
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public class StartupService {
    
    private static final Logger LOG = Logger.getLogger(StartupService.class);
    
    @Value("${username}")
    private String dbName;
    
    @Value("${url}")
    private String dbUrl;
    
    @Value("${email.boss}")
    private String emailBoss;
    
    @Value("${email.office}")
    private String emailOffice;
    
    @Value("${email.all}")
    private String emailAll;
    
    @Value("${email.manager}")
    private String emailManager;
    
    private TestGoogleCalendarService googleCalendarService;

    public StartupService(TestGoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }
    
    
    @PostConstruct
    public void logStartupInfo() throws IOException {
        
        LOG.info("Using database with username " + dbName + " and Url " + dbUrl);
        
        LOG.info("Using following email addresses for notification:");
        LOG.info("Email boss: " + emailBoss);
        LOG.info("Email office: " + emailOffice);
        LOG.info("Email all: " + emailAll);
        LOG.info("Email manager: " + emailManager);
        
        LOG.info("Getting access token for Google Calendar API");
//        googleCalendarService.setUp();
        
    }
    
    
}
