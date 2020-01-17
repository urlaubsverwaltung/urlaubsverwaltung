package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

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
    @PreAuthorize("@userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    @ResponseBody
    public String getCalendarForPerson(HttpServletResponse response, @PathVariable Integer personId, @RequestParam String secret) {

        final String iCal;
        try {
            iCal = personCalendarService.getCalendarForPerson(personId, secret);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for id = " + personId);
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return iCal;
    }

    @GetMapping("/departments/{departmentId}/calendar")
    @ResponseBody
    public String getCalendarForDepartment(HttpServletResponse response, @PathVariable Integer departmentId, @RequestParam String secret) {

        final String iCal;
        try {
            iCal = departmentCalendarService.getCalendarForDepartment(departmentId, secret);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No department found for id = " + departmentId);
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return iCal;
    }

    @GetMapping("/company/calendar")
    @ResponseBody
    public String getCalendarForCompany(HttpServletResponse response, @RequestParam String secret) {

        final String iCal;
        try {
            iCal = companyCalendarService.getCalendarForAll(secret);
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return iCal;
    }

    private void setContentTypeAndHeaders(HttpServletResponse response) {
        response.setContentType("text/calendar");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=calendar.ics");
    }
}
