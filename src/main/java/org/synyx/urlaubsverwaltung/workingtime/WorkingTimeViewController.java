package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web")
public class WorkingTimeViewController {

    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final WorkingTimeService workingTimeService;
    private final WorkingTimeWriteService workingTimeWriteService;
    private final SettingsService settingsService;
    private final WorkingTimeValidator validator;
    private final Clock clock;

    @Autowired
    public WorkingTimeViewController(PersonService personService, WorkingTimeService workingTimeService,
                                     WorkingTimeWriteService workingTimeWriteService, SettingsService settingsService, WorkingTimeValidator validator,
                                     Clock clock) {
        this.personService = personService;
        this.workingTimeService = workingTimeService;
        this.workingTimeWriteService = workingTimeWriteService;
        this.settingsService = settingsService;
        this.validator = validator;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {
        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/person/{personId}/workingtime")
    public String getWorkingTime(@PathVariable("personId") Integer personId, Model model)
        throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        final Optional<WorkingTime> optionalWorkingTime = workingTimeService.getWorkingTime(person, LocalDate.now(clock));
        if (optionalWorkingTime.isPresent()) {
            model.addAttribute("workingTime", new WorkingTimeForm(optionalWorkingTime.get()));
        } else {
            model.addAttribute("workingTime", new WorkingTimeForm());
        }

        fillModel(model, person);

        return "workingtime/workingtime_form";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/person/{personId}/workingtime")
    public String updateWorkingTime(@PathVariable("personId") Integer personId,
                                    @ModelAttribute("workingTime") WorkingTimeForm workingTimeForm,
                                    Model model, Errors errors, RedirectAttributes redirectAttributes) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        validator.validate(workingTimeForm, errors);

        if (errors.hasErrors()) {
            fillModel(model, person);

            return "workingtime/workingtime_form";
        }

        workingTimeWriteService.touch(workingTimeForm.getWorkingDays(), workingTimeForm.getValidFrom(), person, workingTimeForm.getFederalState());

        redirectAttributes.addFlashAttribute("updateSuccess", true);
        return "redirect:/web/person/" + personId;
    }

    private void fillModel(Model model, Person person) {
        model.addAttribute(PERSON_ATTRIBUTE, person);

        final List<WorkingTime> workingTimeHistories = workingTimeService.getByPerson(person);
        final WorkingTime currentWorkingTime = workingTimeService.getWorkingTime(person, LocalDate.now(clock)).orElse(null);
        final FederalState defaultFederalState = settingsService.getSettings().getWorkingTimeSettings().getFederalState();

        model.addAttribute("workingTimeHistories", toWorkingTimeHistoryDtos(currentWorkingTime, workingTimeHistories));
        model.addAttribute("weekDays", DayOfWeek.values());
        model.addAttribute("federalStateTypes", FederalState.federalStatesTypesByCountry());
        model.addAttribute("defaultFederalState", defaultFederalState);
    }

    private List<WorkingTimeHistoryDto> toWorkingTimeHistoryDtos(WorkingTime currentWorkingTime, List<WorkingTime> workingTimes) {
        return workingTimes.stream()
            .map(toWorkingTimeHistoryDto(currentWorkingTime))
            .collect(toList());
    }

    private Function<WorkingTime, WorkingTimeHistoryDto> toWorkingTimeHistoryDto(WorkingTime currentWorkingTime) {
        return workingTime -> {
            final boolean isValid = currentWorkingTime.equals(workingTime);
            final FederalState federalState = workingTime.getFederalState();
            final List<String> workDays = workingTime.getWorkingDays().stream().map(Enum::toString).collect(toList());
            return new WorkingTimeHistoryDto(workingTime.getValidFrom(), workDays, federalState.getCountry(), federalState.toString(), isValid);
        };
    }
}
