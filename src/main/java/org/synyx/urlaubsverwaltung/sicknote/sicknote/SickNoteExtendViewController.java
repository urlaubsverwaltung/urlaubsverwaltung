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
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.format.FormatStyle.FULL;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;
import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.StringUtils.hasText;

@Controller
@RequestMapping("/web/sicknote/extend")
class SickNoteExtendViewController implements HasLaunchpad {

    private final PersonService personService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final SickNoteService sickNoteService;
    private final SickNoteExtensionService sickNoteExtensionService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    SickNoteExtendViewController(PersonService personService, WorkingTimeCalendarService workingTimeCalendarService,
                                 SickNoteService sickNoteService, SickNoteExtensionService sickNoteExtensionService, DateFormatAware dateFormatAware, Clock clock) {
        this.personService = personService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.sickNoteService = sickNoteService;
        this.sickNoteExtensionService = sickNoteExtensionService;
        this.dateFormatAware = dateFormatAware;
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
        final SickNoteExtendDto sickNoteDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), sickNote.getEndDate(), sickNote.getWorkDays(), sickNote.isAubPresent());

        final LocalDate today = LocalDate.now(clock);
        prepareModel(model, signedInUser, today, sickNote, sickNoteDto);

        return "sicknote/sick_note_extend";
    }

    @PostMapping
    public String extendSickNote(@RequestParam(value = "extend", required = false) String extend,
                                 @RequestParam(value = "extendToDate", required = false) LocalDate extendToDate,
                                 // hint if custom date has been submitted or not
                                 @RequestParam(value = "custom-date-preview", required = false) Optional<String> customDateSubmit,
                                 @ModelAttribute("sickNoteExtended") SickNoteExtendDto sickNoteExtendDto, Errors errors,
                                 Model model) {

        final Person signedInUser = personService.getSignedInUser();
        final Optional<SickNote> maybeSickNote = getSickNoteOfYesterdayOrLastWorkDay(signedInUser);
        if (maybeSickNote.isEmpty()) {
            return "sicknote/sick_note_extended_not_found";
        }

        final SickNote sickNote = maybeSickNote.get();

        if (hasText(extend) || customDateSubmit.isPresent()) {
            // form submit with a +x days button or a provided custom date
            prepareSickNoteExtendPreview(signedInUser, sickNote, extend, extendToDate, customDateSubmit, model);
            // TODO use redirect with flashAttributes?
            return "sicknote/sick_note_extend";
        } else {
            // TODO validate sickNoteExtendDto
            sickNoteExtensionService.submitSickNoteExtension(signedInUser, sickNote.getId(), sickNoteExtendDto.endDate(), sickNoteExtendDto.isAub());
            return "redirect:/web/sicknote/" + sickNote.getId();
        }
    }

    private LocalDate nextWorkingDayFollowingTo(Person person, WorkingTimeCalendar calendar, LocalDate date) {
        return calendar.nextWorkingFollowingTo(date)
            .or(() -> workingTimeCalendarService.getNextWorkingDayFollowingTo(person, date))
            .orElseThrow(() -> new IllegalStateException("expected next day to exist in calendar"));
    }

    private WorkingTimeCalendar getWorkingTimeCalendar(SickNote sickNote, LocalDate individualExtendToDate) {

        final Person person = sickNote.getPerson();

        final LocalDate today = LocalDate.now(clock);
        final LocalDate endOfWeek = today.with(nextOrSame(SUNDAY));

        // extended dateRange has a larger end date to have a buffer for calculation of next-working-day
        // possibility of a still non-existent working day is handled by `workingTimeCalendarService.getNextWorkingDayFollowingTo` later
        final LocalDate extendedEnd = max(sickNote.getEndDate(), requireNonNullElse(individualExtendToDate, endOfWeek));
        final DateRange extendedDateRange = new DateRange(sickNote.getStartDate().with(previousOrSame(MONDAY)), extendedEnd.with(nextOrSame(SUNDAY)).with(next(SUNDAY)));

        return getWorkingTimeCalendar(person, extendedDateRange);
    }

    private WorkingTimeCalendar getWorkingTimeCalendar(Person person, DateRange dateRange) {
        final Map<Person, WorkingTimeCalendar> workingTimes = workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), dateRange);
        if (workingTimes.containsKey(person)) {
            return workingTimes.get(person);
        } else {
            // this should not happen, shouldn't it? I'm sorry for that!
            throw new IllegalStateException("Could not find working time calendar for person " + person);
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

    private void prepareModel(Model model, Person signedInUser, LocalDate extendToDate, SickNote sickNote,
                              SickNoteExtendDto currentSickNoteDto) {

        final WorkingTimeCalendar workingTimeCalendar = getWorkingTimeCalendar(sickNote, extendToDate);

        final LocalDate today = LocalDate.now(clock);
        final LocalDate plusOneWorkdayDate = nextWorkingDayFollowingTo(signedInUser, workingTimeCalendar, sickNote.getEndDate());
        final LocalDate plusTwoWorkdaysDate = nextWorkingDayFollowingTo(signedInUser, workingTimeCalendar, plusOneWorkdayDate);

        model.addAttribute("sickNote", currentSickNoteDto);

        model.addAttribute("sickNotePersonId", signedInUser.getId());
        model.addAttribute("today", today);
        model.addAttribute("extendToDate", extendToDate == null ? today : extendToDate);
        model.addAttribute("sickNoteEndDateWord", dateFormatAware.formatWord(currentSickNoteDto.endDate(), FULL));
        model.addAttribute("plusOneWorkdayWord", dateFormatAware.formatWord(plusOneWorkdayDate, FULL));
        model.addAttribute("plusTwoWorkdaysWord", dateFormatAware.formatWord(plusTwoWorkdaysDate, FULL));
        model.addAttribute("untilEndOfWeekWord", dateFormatAware.formatWord(endOfWeek(), FULL));
    }

    private void prepareSickNoteExtendPreview(Person signedInUser, SickNote sickNote, String extend,
                                              LocalDate extendToDate, Optional<String> customDateSubmit, Model model) {

        final WorkingTimeCalendar workingTimeCalendar = getWorkingTimeCalendar(sickNote, extendToDate);
        final LocalDate plusOneWorkdayDate = nextWorkingDayFollowingTo(signedInUser, workingTimeCalendar, sickNote.getEndDate());
        final LocalDate plusTwoWorkdaysDate = nextWorkingDayFollowingTo(signedInUser, workingTimeCalendar, plusOneWorkdayDate);

        final SickNoteExtendDto currentSickNoteDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), sickNote.getEndDate(), sickNote.getWorkDays(), sickNote.isAubPresent());

        prepareModel(model, signedInUser, extendToDate, sickNote, currentSickNoteDto);

        final SickNoteExtendDto sickNoteExtendedDto;
        final String selectedExtend;

        if (customDateSubmit.isPresent()) {
            final BigDecimal nextWorkingDays = workingTimeCalendar.workingTime(sickNote.getStartDate(), extendToDate);
            sickNoteExtendedDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), extendToDate, nextWorkingDays, sickNote.isAubPresent());
            selectedExtend = "custom";
        } else if ("1".equals(extend)) {
            final BigDecimal nextWorkingDays = workingTimeCalendar.workingTime(sickNote.getStartDate(), plusOneWorkdayDate);
            sickNoteExtendedDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), plusOneWorkdayDate, nextWorkingDays, sickNote.isAubPresent());
            selectedExtend = "1";
        } else if ("2".equals(extend)) {
            final BigDecimal nextWorkingDays = workingTimeCalendar.workingTime(sickNote.getStartDate(), plusTwoWorkdaysDate);
            sickNoteExtendedDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), plusTwoWorkdaysDate, nextWorkingDays, sickNote.isAubPresent());
            selectedExtend = "2";
        } else if ("end-of-week".equals(extend)) {
            final LocalDate endOfWeek = endOfWeek();
            final BigDecimal nextWorkingDays = workingTimeCalendar.workingTime(sickNote.getStartDate(), endOfWeek);
            sickNoteExtendedDto = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate(), endOfWeek, nextWorkingDays, sickNote.isAubPresent());
            selectedExtend = "end-of-week";
        } else {
            sickNoteExtendedDto = null;
            selectedExtend = "";
        }

        model.addAttribute("sickNoteExtended", sickNoteExtendedDto);
        model.addAttribute("selectedExtend", selectedExtend);
    }

    private LocalDate max(LocalDate date1, LocalDate date2) {
        if (date1.isAfter(date2)) {
            return date1;
        }
        return date2;
    }

    private LocalDate endOfWeek() {
        return LocalDate.now(clock).with(nextOrSame(SUNDAY));
    }
}
