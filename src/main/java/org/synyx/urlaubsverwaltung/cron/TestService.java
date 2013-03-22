/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.cron;

import java.io.IOException;
import org.springframework.scheduling.annotation.Scheduled;
import org.synyx.urlaubsverwaltung.calendar.GoogleCalendarService;
import org.synyx.urlaubsverwaltung.person.PersonService;

/**
 *
 * @author fraulyoner
 */
public class TestService {
    
    private GoogleCalendarService calService;

    public TestService(GoogleCalendarService calService) {
        this.calService = calService;
    }

//    @Scheduled(cron = "0 0 * * * *")
//    void sendResult() throws IOException {
//
//        calService.addEvent();
//    }
    
}
