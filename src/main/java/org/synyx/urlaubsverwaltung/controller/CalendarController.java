/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.calendar.GoogleCalendarServiceImpl;
import org.synyx.urlaubsverwaltung.calendar.YearWeek;
import org.synyx.urlaubsverwaltung.service.PersonService;


/**
 * @author  aljona
 *
 *          <p>dies ist ein vorläufiger versuch den google calendar einzubinden die methoden wurden dem
 *          googlecalendarserviceimpl des ressourcenplanungstools von Otto Allmendinger - allmendinger@synyx.de
 *          entnommen für das urlaubsverwaltungstool nicht nötige methoden und attribute wurden auskommentiert</p>
 */
public class CalendarController {

    public static final String BASE_URL = "/calendar";

    /*
     * private static final String SYNYX_VACATION_CALENDAR_URL = GOOGLE_BASE_URL +
     * "synyx.de_2d39373137323636363330@resource.calendar.google.com/private/full";
     */
    private final GoogleCalendarServiceImpl googleCalendarService;
    private final PersonService personService;

    public CalendarController(PersonService personService, GoogleCalendarServiceImpl googleCalendarServiceImpl) {

        this.personService = personService;
        this.googleCalendarService = googleCalendarServiceImpl;
    }

    @RequestMapping(value = BASE_URL + "/week-minutes", method = RequestMethod.GET)
    public String getWeekMinutes(@RequestParam(value = "yearWeek", required = false) YearWeek yearWeek, Model model) {

        if (yearWeek == null) {
            return "redirect:" + BASE_URL + "/week-minutes?yearWeek=" + (new YearWeek()).toString();
        } else {
            model.addAttribute("yearWeek", yearWeek);

            return "calendar/participation";
        }
    }

//    @RequestMapping(value = BASE_URL + "/week-minutes.json", method = RequestMethod.GET)
//    @ResponseBody
//    public Map<String, Object> getWeekMinutes(@RequestParam YearWeek yearWeek, @RequestParam String calendarId)
//        throws AuthenticationException, IOException, ServiceException {
//
//        Map<String, Object> response = Maps.newHashMap();
//
//        CalendarEventFeed eventFeed = googleCalendarService.getCalendarEventFeed(calendarId, yearWeek);
//
//        int[] weekMinutes = googleCalendarService.getWeekMinutes(eventFeed.getEntries(), yearWeek);
//
//        response.put("name", eventFeed.getTitle().getPlainText());
//        response.put("weekMinutes", weekMinutes);
//
//        return response;
//    }

//    @RequestMapping(value = BASE_URL + "/week-minutes/by-person.json", method = RequestMethod.GET)
//    @ResponseBody
//    public Map<String, Object> getWeekMinutesByPerson(@RequestParam YearWeek yearWeek, @RequestParam String calendarId)
//        throws AuthenticationException, IOException, ServiceException {
//
//        CalendarEventFeed eventFeed = googleCalendarService.getCalendarEventFeed(calendarId, yearWeek);
//
//        Map<Long, int[]> weekMinutes = Maps.newHashMap();
//
//        for (Person person : personService.getAllPersons()) {
//            weekMinutes.put(person.getId(),
//                googleCalendarService.getWeekMinutes(eventFeed.getEntries(), yearWeek, person));
//        }
//
//        Map<String, Object> response = Maps.newHashMap();
//
//        response.put("name", eventFeed.getTitle().getPlainText());
//        response.put("weekMinutes", weekMinutes);
//
//        return response;
//    }
}
