package org.synyx.urlaubsverwaltung.calendar.web;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;


@Controller
@RequestMapping("/web/calendar")
public class CalendarSharingViewController {

    private final PersonService personService;

    private String calendarUrl;

    @Autowired
    public CalendarSharingViewController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/share")
    public String index(Model model) {

        Person signedInUser = personService.getSignedInUser();

        PrivateCalendarShareDto dto = new PrivateCalendarShareDto();
        dto.setPersonId(signedInUser.getId());
        if (!Strings.isEmpty(calendarUrl)) {
            dto.setCalendarUrl(calendarUrl);
        }

        model.addAttribute("privateCalendarShare", dto);

        return "calendarsharing/index";
    }

    @PostMapping(value = "/me/share")
    public String linkPrivateCalendar() {

        calendarUrl = "https://urlaubsverwaltung.cloud/calendar/asdfjsudfgjdlakjlaksjrdsakljfsdjaf";

        return "redirect:/web/calendar/share";
    }

    @PostMapping(value = "/me/share", params = "unlink")
    public String unlinkPrivateCalendar() {

        calendarUrl = "";

        return "redirect:/web/calendar/share";
    }
}
