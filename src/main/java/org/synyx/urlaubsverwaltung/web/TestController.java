
package org.synyx.urlaubsverwaltung.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.synyx.urlaubsverwaltung.calendar.GoogleCalendarService;

import java.io.IOException;

/**
 * @author  Aljona Murygina
 */
@Controller
public class TestController {
    
    private static final String JSP_FOLDER = "calendar/";
    
    private GoogleCalendarService googleCalendarService;

    public TestController(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }
    
    @RequestMapping(value = "/calendar", method = RequestMethod.GET)
    public String getCalendarSite(Model model){

        return JSP_FOLDER + "calendar";
    }
    
    @RequestMapping(value = "/calendar/setup", method = RequestMethod.GET)
    public String setupGoogleCalendar(Model model) throws IOException {

        googleCalendarService.setUp();
        
        return JSP_FOLDER + "setup";
    }
    
    @RequestMapping(value = "/calendar/event", method = RequestMethod.GET)
    public String addEvent(Model model) throws IOException {

        googleCalendarService.addEvent();
        
        return JSP_FOLDER + "event";
    }
    
}
