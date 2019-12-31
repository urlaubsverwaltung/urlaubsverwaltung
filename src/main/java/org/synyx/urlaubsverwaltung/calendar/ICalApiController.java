package org.synyx.urlaubsverwaltung.calendar;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


@Api("calendar")
@RestController
@RequestMapping("/api")
public class ICalApiController {

    private final ICalService iCalService;

    @Autowired
    public ICalApiController(ICalService iCalService) {

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
        }

        response.setContentType("text/calendar");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=calendar.ics");

        return iCal;
    }

    @GetMapping("/departments/{departmentId}/calendar")
    public String getCalendarForDepartment(HttpServletResponse response, @PathVariable Integer departmentId) {

        final String iCal;
        try {
            iCal = iCalService.getCalendarForDepartment(departmentId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No department found for id = " + departmentId);
        }

        response.setContentType("text/calendar");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=calendar.ics");

        return iCal;
    }

}
