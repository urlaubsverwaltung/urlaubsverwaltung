package org.synyx.urlaubsverwaltung.calendar;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;


@Api("calendar")
@Controller
@RequestMapping("/web")
public class ICalViewController {

    private final ICalService iCalService;

    @Autowired
    public ICalViewController(ICalService iCalService) {

        this.iCalService = iCalService;
    }

    @GetMapping("/persons/{personId}/calendar")
    @PreAuthorize("@userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String getCalendarForPerson(HttpServletResponse response, @PathVariable Integer personId) {

        final String iCal;
        try {
            iCal = iCalService.getCalendarForPerson(personId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for id = " + personId);
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return iCal;
    }

    @GetMapping("/departments/{departmentId}/calendar")
    public String getCalendarForDepartment(HttpServletResponse response, @PathVariable Integer departmentId) {

        final String iCal;
        try {
            iCal = iCalService.getCalendarForDepartment(departmentId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No department found for id = " + departmentId);
        } catch (CalendarException e) {
            throw new ResponseStatusException(NO_CONTENT);
        }

        setContentTypeAndHeaders(response);

        return iCal;
    }

    @GetMapping("/company/calendar")
    public String getCalendarForCompany(HttpServletResponse response) {

        final String iCal;
        try {
            iCal = iCalService.getCalendarForAll();
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
