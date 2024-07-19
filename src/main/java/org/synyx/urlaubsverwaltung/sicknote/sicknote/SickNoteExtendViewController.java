package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@Controller
@RequestMapping("/web/sicknote/extend")
class SickNoteExtendViewController implements HasLaunchpad {

    private final PersonService personService;
    private final SickNoteService sickNoteService;
    private final Clock clock;

    SickNoteExtendViewController(PersonService personService, SickNoteService sickNoteService, Clock clock) {
        this.personService = personService;
        this.sickNoteService = sickNoteService;
        this.clock = clock;
    }

    @GetMapping
    public String extendSickNoteView(Model model) {

        final Person signedInUser = personService.getSignedInUser();
        final Optional<SickNote> maybeSickNote = getSickNoteOfYesterdayOrLastWorkDay(signedInUser);
        if (maybeSickNote.isEmpty()) {
            return "sicknote/sick_note_extended_not_found";
        }

        final SickNote sickNote = maybeSickNote.get();

        // TODO use correct workingdays value
        final SickNoteExtendDto sickNoteDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), sickNote.getEndDate(), 42, sickNote.isAubPresent());

        model.addAttribute("sickNote", sickNoteDto);
        model.addAttribute("sickNotePersonId", signedInUser.getId());
        model.addAttribute("today", LocalDate.now(clock));
        model.addAttribute("extendToDate", LocalDate.now(clock));
        // TODO model attributes
        model.addAttribute("sickNoteEndDateWord", "heute");
        model.addAttribute("extensionDatePlusOne", LocalDate.now());
        model.addAttribute("extensionDatePlusTwo", LocalDate.now().plusDays(1));
        model.addAttribute("extensionDateEndOfWeek", LocalDate.now().plusDays(4));
        model.addAttribute("plusOneWorkdayWord", "heute");
        model.addAttribute("plusTwoWorkdaysWord", "morgen");
        model.addAttribute("untilEndOfWeekWord", "xxx");

        return "sicknote/sick_note_extend";
    }

    @PostMapping
    public String extendSickNote(@RequestParam(value = "extend", required = false) String extend,
                                 @RequestParam(value = "extendToDate", required = false) LocalDate extendToDate,
                                 @RequestParam(value = "custom-date-preview", required = false) Optional<String> customDateSubmit,
                                 @ModelAttribute("sickNoteExtended") SickNoteExtendDto sickNoteExtendDto, Errors errors,
                                 Model model) {

        final Person signedInUser = personService.getSignedInUser();
        final Optional<SickNote> maybeSickNote = getSickNoteOfYesterdayOrLastWorkDay(signedInUser);
        if (maybeSickNote.isEmpty()) {
            return "sicknote/sick_note_extended_not_found";
        }

        if (hasText(extend) || customDateSubmit.isPresent()) {
            // form submit with a +x button or a custom date
            final SickNote sickNote = maybeSickNote.get();
            return sickNoteExtendPreview(signedInUser, sickNote, extend, extendToDate, customDateSubmit, model);
        } else {
            // form submit to extend the sick note
            return extendSickNote(signedInUser, sickNoteExtendDto, errors, model);
        }
    }


    private Optional<SickNote> getSickNoteOfYesterdayOrLastWorkDay(Person signedInUser) {
        final Optional<SickNote> maybeSickNote = sickNoteService.getSickNoteOfYesterdayOrLastWorkDay(signedInUser);
        if (maybeSickNote.isPresent()) {
            final SickNote sickNote = maybeSickNote.get();
            if (!sickNote.getPerson().equals(signedInUser)) {
                throw new AccessDeniedException("SickNote is not of User '%s'".formatted(signedInUser.getId()));
            }
        }
        return maybeSickNote;
    }

    /**
     * Handles preview rendering of the desired sick note to extend.
     */
    private String sickNoteExtendPreview(Person signedInUser, SickNote sickNote, String extend, LocalDate extendToDate, Optional<String> customDateSubmit, Model model) {

        // TODO use correct workingdays value
        final SickNoteExtendDto currentSickNoteDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), sickNote.getEndDate(), 42, sickNote.isAubPresent());

        model.addAttribute("sickNote", currentSickNoteDto);
        model.addAttribute("sickNotePersonId", signedInUser.getId());
        model.addAttribute("today", LocalDate.now(clock));
        model.addAttribute("extendToDate", extendToDate == null ? LocalDate.now(clock) : extendToDate);
        // TODO model attributes
        model.addAttribute("sickNoteEndDateWord", "heute");
        model.addAttribute("extensionDatePlusOne", LocalDate.now());
        model.addAttribute("extensionDatePlusTwo", LocalDate.now().plusDays(1));
        model.addAttribute("extensionDateEndOfWeek", LocalDate.now().plusDays(4));
        model.addAttribute("plusOneWorkdayWord", "heute");
        model.addAttribute("plusTwoWorkdaysWord", "morgen");
        model.addAttribute("untilEndOfWeekWord", "xxx");

        final SickNoteExtendDto sickNoteExtendedDto;

        // TODO use correct values
        if ("1".equals(extend)) {
            model.addAttribute("selectedExtend", "1");
            sickNoteExtendedDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), sickNote.getEndDate().plusDays(1), 2, sickNote.isAubPresent());
        } else if ("2".equals(extend)) {
            model.addAttribute("selectedExtend", "2");
            sickNoteExtendedDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), sickNote.getEndDate().plusDays(2), 3, sickNote.isAubPresent());
        } else if ("end-of-week".equals(extend)) {
            model.addAttribute("selectedExtend", "end-of-week");
            sickNoteExtendedDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), sickNote.getEndDate().plusDays(1), 4, sickNote.isAubPresent());
        } else if (customDateSubmit.isPresent()) {
            model.addAttribute("selectedExtend", "custom");
            sickNoteExtendedDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), extendToDate, 5, sickNote.isAubPresent());
        } else {
            model.addAttribute("selectedExtend", "");
            sickNoteExtendedDto = null;
        }

        model.addAttribute("sickNoteExtended", sickNoteExtendedDto);

        // TODO use redirect with flashAttributes?
        return "sicknote/sick_note_extend";
    }

    /**
     * Extends the sick note with the desired information.
     */
    private String extendSickNote(Person signedInUser, SickNoteExtendDto sickNoteExtendDto, Errors errors, Model model) {
        // TODO extend sick note
        throw new RuntimeException("not implemented yet");
    }
}
