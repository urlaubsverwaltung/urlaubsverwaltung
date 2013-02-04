
package org.synyx.urlaubsverwaltung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.synyx.urlaubsverwaltung.calendar.TestGoogleCalendarService;

import java.io.IOException;

/**
 * @author  Aljona Murygina
 */
@Controller
public class TestController {
    
    private TestGoogleCalendarService googleCalendarService;

    public TestController(TestGoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }
    
    @RequestMapping(value = "/calendar", method = RequestMethod.GET)
    public String addEvent(Model model) throws IOException {

//        googleCalendarService.addEvent();
        
        return "test";
    }
    
}
