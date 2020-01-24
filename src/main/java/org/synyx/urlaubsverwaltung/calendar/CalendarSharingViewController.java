package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static java.lang.String.format;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;


@Controller
@RequestMapping("/web/persons/{personId}/calendar/share")
public class CalendarSharingViewController {

    private final PersonCalendarService personCalendarService;

    @Autowired
    public CalendarSharingViewController(PersonCalendarService personCalendarService) {
        this.personCalendarService = personCalendarService;
    }

    @GetMapping
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String index(@PathVariable int personId, Model model, HttpServletRequest request) {

        final PersonCalendarDto dto = new PersonCalendarDto();
        dto.setPersonId(personId);

        final Optional<PersonCalendar> maybePersonCalendar = personCalendarService.getPersonCalendar(personId);
        if (maybePersonCalendar.isPresent()) {
            final PersonCalendar personCalendar = maybePersonCalendar.get();
            final String url = format("%s://%s/web/persons/%d/calendar?secret=%s",
                request.getScheme(), request.getHeader("host"), personId, personCalendar.getSecret());

            dto.setCalendarUrl(url);
        }

        model.addAttribute("privateCalendarShare", dto);

        return "calendarsharing/index";
    }

    @PostMapping(value = "/me")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String linkPrivateCalendar(@PathVariable int personId) {

        personCalendarService.createCalendarForPerson(personId);

        return format("redirect:/web/persons/%d/calendar/share", personId);
    }

    @PostMapping(value = "/me", params = "unlink")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String unlinkPrivateCalendar(@PathVariable int personId) {

        personCalendarService.deletePersonalCalendarForPerson(personId);

        return format("redirect:/web/persons/%d/calendar/share", personId);
    }
}
