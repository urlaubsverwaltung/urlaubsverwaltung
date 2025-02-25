package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;
import static java.util.Objects.requireNonNullElse;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.web.HotwiredTurboConstants.HEADER_TURBO_REQUEST_ID;

@Controller
@RequestMapping("/web/sicknote/extend")
class SickNoteExtendViewController implements HasLaunchpad {

    private final PersonService personService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final SickNoteService sickNoteService;
    private final SickNoteExtensionServiceImpl sickNoteExtensionService;
    private final SickNoteExtensionInteractionService sickNoteExtensionInteractionService;
    private final SickNoteExtendValidator sickNoteExtendValidator;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    SickNoteExtendViewController(PersonService personService, WorkingTimeCalendarService workingTimeCalendarService,
                                 SickNoteService sickNoteService, SickNoteExtensionServiceImpl sickNoteExtensionService,
                                 SickNoteExtensionInteractionService sickNoteExtensionInteractionService,
                                 SickNoteExtendValidator sickNoteExtendValidator, DateFormatAware dateFormatAware, Clock clock) {
        this.personService = personService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.sickNoteService = sickNoteService;
        this.sickNoteExtensionService = sickNoteExtensionService;
        this.sickNoteExtensionInteractionService = sickNoteExtensionInteractionService;
        this.sickNoteExtendValidator = sickNoteExtendValidator;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
    }

    @GetMapping
    public String extendSickNoteView(Model model) {

        final Person signedInUser = personService.getSignedInUser();
        final Optional<SickNote> maybeSickNote = getSickNoteOfYesterdayOrLastWorkDay(signedInUser);
        if (maybeSickNote.isEmpty() || maybeSickNote.get().getDayLength().isHalfDay()) {
            return "sicknote/sick_note_extended_not_found";
        }

        final SickNote sickNote = maybeSickNote.get();
        final LocalDate today = LocalDate.now(clock);
        prepareModel(model, signedInUser, today, sickNote);

        final SickNoteExtendDto sickNoteExtension = new SickNoteExtendDto(sickNote.getId(), sickNote.getStartDate());
        model.addAttribute("sickNoteExtension", sickNoteExtension);

        return "sicknote/sick_note_extend";
    }

    @PostMapping
    public String extendSickNote(@RequestParam(value = "extend", required = false) String extend,
                                 @RequestParam(value = "extendToDate", required = false) LocalDate extendToDate,
                                 // hint if custom date has been submitted or not
                                 @RequestParam(value = "custom-date-preview", required = false) Optional<String> customDateSubmit,
                                 @ModelAttribute("sickNoteExtension") SickNoteExtendDto sickNoteExtendDto, Errors errors,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 @RequestHeader(value = HEADER_TURBO_REQUEST_ID, required = false) Optional<UUID> turboRequestId,
                                 HttpServletResponse response) {

        final boolean hasUserSelectedDays = hasText(extend);
        final boolean hasUserSelectedCustomDate = customDateSubmit.isPresent();
        final boolean isCreateExtendSubmit = !hasUserSelectedCustomDate && !hasUserSelectedDays;

        final Person signedInUser = personService.getSignedInUser();
        final Optional<SickNote> maybeSickNote = getSickNoteOfYesterdayOrLastWorkDay(signedInUser);
        if (maybeSickNote.isEmpty() || maybeSickNote.get().getDayLength().isHalfDay()) {
            return "sicknote/sick_note_extended_not_found";
        }

        if (!hasUserSelectedCustomDate) {
            // unset value when form has not been submitted with choosing custom date
            // to avoid using this variable accidentally
            extendToDate = null;
            sickNoteExtendDto.setExtendToDate(null);
        }

        final SickNote sickNote = maybeSickNote.get();

        if (isCreateExtendSubmit && sickNoteExtendDto.getSickNoteId() == null) {
            // form has been submitted with 'report sick' without filling the form actually
            // therefore just redirect to page
            // we could disable the submit button until the form is ready, this is bad practice however
            // because of users clicking these buttons and nothing happens for instance (https://axesslab.com/disabled-buttons-suck)
            redirectAttributes.addFlashAttribute("showFillFormFeedback", true);
            return "redirect:/web/sicknote/extend";
        }

        if (hasUserSelectedCustomDate || isCreateExtendSubmit) {
            SickNoteExtendDto mutationGuard = sickNoteExtendDto;
            if (hasUserSelectedCustomDate) {
                mutationGuard = new SickNoteExtendDto(sickNoteExtendDto);
                mutationGuard.setEndDate(extendToDate);
            }
            sickNoteExtendValidator.validate(mutationGuard, errors);
        }
        if (errors.hasErrors()) {
            prepareSickNoteExtendPreview(signedInUser, maybeSickNote.get(), sickNoteExtendDto, extend, extendToDate, customDateSubmit, model);
            if (turboRequestId.isPresent()) {
                response.setStatus(UNPROCESSABLE_ENTITY.value());
            }
            return "sicknote/sick_note_extend";
        }

        if (isCreateExtendSubmit) {

            sickNoteExtensionInteractionService.submitSickNoteExtension(signedInUser, sickNote.getId(), sickNoteExtendDto.getEndDate());

            if (signedInUser.hasAnyRole(OFFICE, SICK_NOTE_ADD)) {
                sickNoteExtensionInteractionService.acceptSubmittedExtension(signedInUser, sickNote.getId(), null);
            }

            redirectAttributes.addFlashAttribute("showExtensionCreatedFeedback", true);
            return "redirect:/web/sicknote/" + sickNote.getId();
        } else {
            prepareSickNoteExtendPreview(signedInUser, sickNote, sickNoteExtendDto, extend, extendToDate, customDateSubmit, model);
            if (turboRequestId.isPresent()) {
                // tricking @hotwired/turbo with redirect status code to render like we want to :x
                // since not doing a real redirect. location is missing...
                // (actually we should refactor this POST process to redirection in js case and without js)
                response.setStatus(FOUND.value());
            }
            return "sicknote/sick_note_extend";
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
        final LocalDate extendedEnd = Collections.max(List.of(sickNote.getEndDate(), requireNonNullElse(individualExtendToDate, endOfWeek)));
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

    private void prepareModel(Model model, Person signedInUser, LocalDate extendToDate, SickNote sickNote) {

        final Optional<SickNoteExtension> existingExtension = sickNoteExtensionService.findSubmittedExtensionOfSickNote(sickNote);
        existingExtension.ifPresent(extension ->
            model.addAttribute("existingExtensionEndDate", extension.nextEndDate())
        );

        final WorkingTimeCalendar workingTimeCalendar = getWorkingTimeCalendar(sickNote, extendToDate);

        final LocalDate today = LocalDate.now(clock);
        final LocalDate plusOneWorkdayDate = nextWorkingDayFollowingTo(signedInUser, workingTimeCalendar, sickNote.getEndDate());
        final LocalDate plusTwoWorkdaysDate = nextWorkingDayFollowingTo(signedInUser, workingTimeCalendar, plusOneWorkdayDate);

        model.addAttribute("sickNotePersonId", signedInUser.getId());
        model.addAttribute("today", today);
        model.addAttribute("sickNoteTypeChild", sickNote.getSickNoteType().getCategory().equals(SICK_NOTE_CHILD));
        model.addAttribute("extendToDate", extendToDate == null ? today : extendToDate);
        model.addAttribute("sickNoteEndDateWord", dateFormatAware.formatWord(sickNote.getEndDate(), FormatStyle.FULL));
        model.addAttribute("plusOneWorkdayWord", dateFormatAware.formatWord(plusOneWorkdayDate, FormatStyle.FULL));
        model.addAttribute("plusTwoWorkdaysWord", dateFormatAware.formatWord(plusTwoWorkdaysDate, FormatStyle.FULL));
        model.addAttribute("untilEndOfWeekWord", dateFormatAware.formatWord(endOfWeek(), FormatStyle.FULL));
    }

    private void prepareSickNoteExtendPreview(Person signedInUser, SickNote sickNote, SickNoteExtendDto sickNoteExtendDto, String extend,
                                              LocalDate extendToDate, Optional<String> customDateSubmit, Model model) {

        final WorkingTimeCalendar workingTimeCalendar = getWorkingTimeCalendar(sickNote, extendToDate);
        final LocalDate plusOneWorkdayDate = nextWorkingDayFollowingTo(signedInUser, workingTimeCalendar, sickNote.getEndDate());
        final LocalDate plusTwoWorkdaysDate = nextWorkingDayFollowingTo(signedInUser, workingTimeCalendar, plusOneWorkdayDate);

        prepareModel(model, signedInUser, extendToDate, sickNote);

        final SickNoteExtendPreviewDto sickNotePreviewNext;
        final String selectedExtend;

        if (customDateSubmit.isPresent()) {
            final BigDecimal nextWorkingDays = workingTimeCalendar.workingTime(sickNote.getStartDate(), extendToDate);
            sickNotePreviewNext = new SickNoteExtendPreviewDto(sickNote.getStartDate(), extendToDate, nextWorkingDays);
            selectedExtend = "custom";
        } else if ("1".equals(extend)) {
            final BigDecimal nextWorkingDays = workingTimeCalendar.workingTime(sickNote.getStartDate(), plusOneWorkdayDate);
            sickNotePreviewNext = new SickNoteExtendPreviewDto(sickNote.getStartDate(), plusOneWorkdayDate, nextWorkingDays);
            selectedExtend = "1";
        } else if ("2".equals(extend)) {
            final BigDecimal nextWorkingDays = workingTimeCalendar.workingTime(sickNote.getStartDate(), plusTwoWorkdaysDate);
            sickNotePreviewNext = new SickNoteExtendPreviewDto(sickNote.getStartDate(), plusTwoWorkdaysDate, nextWorkingDays);
            selectedExtend = "2";
        } else if ("end-of-week".equals(extend)) {
            final LocalDate endOfWeek = endOfWeek();
            final BigDecimal nextWorkingDays = workingTimeCalendar.workingTime(sickNote.getStartDate(), endOfWeek);
            sickNotePreviewNext = new SickNoteExtendPreviewDto(sickNote.getStartDate(), endOfWeek, nextWorkingDays);
            selectedExtend = "end-of-week";
        } else {
            sickNotePreviewNext = null;
            selectedExtend = "";
        }

        final SickNoteExtendPreviewDto sickNotePreviewCurrent =
            new SickNoteExtendPreviewDto(sickNote.getStartDate(), sickNote.getEndDate(), sickNote.getWorkDays());

        model.addAttribute("sickNotePreviewCurrent", sickNotePreviewCurrent);
        model.addAttribute("sickNotePreviewNext", sickNotePreviewNext);
        model.addAttribute("selectedExtend", selectedExtend);

        if (sickNotePreviewNext != null) {
            sickNoteExtendDto.setEndDate(sickNotePreviewNext.endDate());
            model.addAttribute("sickNoteExtension", sickNoteExtendDto);
        }
    }

    private LocalDate endOfWeek() {
        return LocalDate.now(clock).with(nextOrSame(SUNDAY));
    }
}
