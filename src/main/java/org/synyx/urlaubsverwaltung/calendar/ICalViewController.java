package org.synyx.urlaubsverwaltung.calendar;

import io.swagger.v3.oas.annotations.Hidden;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Hidden
@Controller
@RequestMapping("/web")
public class ICalViewController {

    private final PersonCalendarService personCalendarService;
    private final DepartmentCalendarService departmentCalendarService;
    private final CompanyCalendarService companyCalendarService;
    private final CalendarOutputter calendarOutputter;

    @Autowired
    public ICalViewController(PersonCalendarService personCalendarService, DepartmentCalendarService departmentCalendarService,
                              CompanyCalendarService companyCalendarService) {

        this.personCalendarService = personCalendarService;
        this.departmentCalendarService = departmentCalendarService;
        this.companyCalendarService = companyCalendarService;
        this.calendarOutputter = new CalendarOutputter();
    }

    @GetMapping("/persons/{personId}/calendar")
    @ResponseBody
    public void getCalendarForPerson(Locale locale, HttpServletResponse response, @PathVariable Integer personId, @RequestParam String secret) {

        setContentTypeAndHeaders(response);

        try {
            final Calendar calendar = personCalendarService.getCalendarForPerson(personId, secret, locale);
            calendarOutputter.output(calendar, response.getOutputStream());
        } catch (IOException | IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not generate person calendar");
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }
    }

    @GetMapping("/departments/{departmentId}/persons/{personId}/calendar")
    @ResponseBody
    public void getCalendarForDepartment(Locale locale, HttpServletResponse response, @PathVariable Integer departmentId, @PathVariable Integer personId, @RequestParam String secret) {

        setContentTypeAndHeaders(response);

        try {
            final Calendar calendar = departmentCalendarService.getCalendarForDepartment(departmentId, personId, secret, locale);
            calendarOutputter.output(calendar, response.getOutputStream());
        } catch (IOException | IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not generate department calendar");
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }
    }

    @GetMapping("/company/persons/{personId}/calendar")
    @ResponseBody
    public void getCalendarForCompany(Locale locale, HttpServletResponse response, @PathVariable Integer personId, @RequestParam String secret) {

        setContentTypeAndHeaders(response);

        try {
            final Calendar calendar = companyCalendarService.getCalendarForAll(personId, secret, locale);
            calendarOutputter.output(calendar, response.getOutputStream());
        } catch (IOException | IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not generate company calendar");
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }
    }

    private void setContentTypeAndHeaders(HttpServletResponse response) {
        response.setContentType("text/calendar");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=calendar.ics");
    }
}
