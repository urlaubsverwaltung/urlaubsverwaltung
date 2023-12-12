package org.synyx.urlaubsverwaltung.calendar;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    ICalViewController(
        PersonCalendarService personCalendarService,
        DepartmentCalendarService departmentCalendarService,
        CompanyCalendarService companyCalendarService
    ) {
        this.personCalendarService = personCalendarService;
        this.departmentCalendarService = departmentCalendarService;
        this.companyCalendarService = companyCalendarService;
    }

    @GetMapping("/persons/{personId}/calendar")
    @ResponseBody
    public String getCalendarForPerson(Locale locale, HttpServletResponse response, @PathVariable Long personId, @RequestParam String secret) {

        final ByteArrayResource iCal;
        try {
            iCal = personCalendarService.getCalendarForPerson(personId, secret, locale);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not generate person calendar");
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return new String(iCal.getByteArray());
    }

    @GetMapping("/departments/{departmentId}/persons/{personId}/calendar")
    @ResponseBody
    public String getCalendarForDepartment(Locale locale, HttpServletResponse response, @PathVariable Long departmentId, @PathVariable Long personId, @RequestParam String secret) {

        final ByteArrayResource iCal;
        try {
            iCal = departmentCalendarService.getCalendarForDepartment(departmentId, personId, secret, locale);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not generate department calendar");
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return new String(iCal.getByteArray());
    }

    @GetMapping("/company/persons/{personId}/calendar")
    @ResponseBody
    public String getCalendarForCompany(Locale locale, HttpServletResponse response, @PathVariable Long personId, @RequestParam String secret) {

        final ByteArrayResource iCal;
        try {
            iCal = companyCalendarService.getCalendarForAll(personId, secret, locale);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not generate company calendar");
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return new String(iCal.getByteArray());
    }

    private void setContentTypeAndHeaders(HttpServletResponse response) {
        response.setContentType("text/calendar");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=calendar.ics");
    }
}
