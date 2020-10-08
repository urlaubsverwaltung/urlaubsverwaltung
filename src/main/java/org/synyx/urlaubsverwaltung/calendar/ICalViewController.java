package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Controller
@RequestMapping("/web")
public class ICalViewController {

    private final PersonCalendarService personCalendarService;
    private final DepartmentCalendarService departmentCalendarService;
    private final CompanyCalendarService companyCalendarService;

    @Autowired
    public ICalViewController(PersonCalendarService personCalendarService, DepartmentCalendarService departmentCalendarService,
                              CompanyCalendarService companyCalendarService) {

        this.personCalendarService = personCalendarService;
        this.departmentCalendarService = departmentCalendarService;
        this.companyCalendarService = companyCalendarService;
    }

    @GetMapping("/persons/{personId}/calendar")
    @ResponseBody
    public String getCalendarForPerson(Locale locale, HttpServletResponse response, @PathVariable Integer personId, @RequestParam String secret) {

        final File iCal;
        try {
            iCal = personCalendarService.getCalendarForPerson(personId, secret, locale);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for id = " + personId);
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return fileToString(iCal);
    }

    @GetMapping("/departments/{departmentId}/persons/{personId}/calendar")
    @ResponseBody
    public String getCalendarForDepartment(Locale locale, HttpServletResponse response, @PathVariable Integer departmentId, @PathVariable Integer personId, @RequestParam String secret) {

        final File iCal;
        try {
            iCal = departmentCalendarService.getCalendarForDepartment(departmentId, personId, secret, locale);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No department found for id = " + departmentId);
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return fileToString(iCal);
    }

    @GetMapping("/company/persons/{personId}/calendar")
    @ResponseBody
    public String getCalendarForCompany(Locale locale, HttpServletResponse response, @PathVariable Integer personId, @RequestParam String secret) {

        final File iCal;
        try {
            iCal = companyCalendarService.getCalendarForAll(personId, secret, locale);
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return fileToString(iCal);
    }

    private String fileToString(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }
    }

    private void setContentTypeAndHeaders(HttpServletResponse response) {
        response.setContentType("text/calendar");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=calendar.ics");
    }
}
