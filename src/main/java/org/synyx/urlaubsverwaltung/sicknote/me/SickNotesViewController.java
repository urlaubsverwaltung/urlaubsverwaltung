package org.synyx.urlaubsverwaltung.sicknote.me;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Comparator.comparing;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;


@Controller
@RequestMapping("/")
public class SickNotesViewController implements HasLaunchpad {

    private static final String PERSON_ATTRIBUTE = "person";
    public static final String MY_SICKNOTES_ANONYMOUS_PATH = "/web/persons/me/sicknotes";
    public static final String MY_SICKNOTES_PATH = "/web/persons/{personId}/sicknotes";

    private final PersonService personService;
    private final WorkDaysCountService workDaysCountService;
    private final SickNoteService sickNoteService;
    private final DepartmentService departmentService;
    private final Clock clock;

    @Autowired
    public SickNotesViewController(
        PersonService personService,
        WorkDaysCountService workDaysCountService,
        SickNoteService sickNoteService,
        DepartmentService departmentService,
        Clock clock
    ) {
        this.personService = personService;
        this.workDaysCountService = workDaysCountService;
        this.sickNoteService = sickNoteService;
        this.departmentService = departmentService;
        this.clock = clock;
    }

    @GetMapping(MY_SICKNOTES_ANONYMOUS_PATH)
    public String showMySickNotes(@RequestParam(value = "year", required = false) String year) {
        final Person signedInUser = personService.getSignedInUser();
        if (hasText(year)) {
            return "redirect:/web/persons/" + signedInUser.getId() + "/sicknotes?year=" + year;
        }

        return "redirect:/web/persons/" + signedInUser.getId() + "/sicknotes";
    }

    @GetMapping(MY_SICKNOTES_PATH)
    public String showMySickNotes(
        @PathVariable("personId") Long personId,
        @RequestParam(value = "year", required = false) Integer year,
        Model model
    )
        throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        final Person signedInUser = personService.getSignedInUser();

        model.addAttribute(PERSON_ATTRIBUTE, person);
        model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(person));

        final LocalDate now = LocalDate.now(clock);
        final int yearToShow = year == null ? now.getYear() : year;

        prepareSickNoteList(person, yearToShow, model);

        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("selectedYear", yearToShow);
        model.addAttribute("currentMonth", now.getMonthValue());
        model.addAttribute("signedInUser", signedInUser);

        model.addAttribute("canViewSickNoteAnotherUser", signedInUser.hasRole(OFFICE)
            || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_VIEW, person)
            || departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person));

        return "me/sicknotes";
    }

    private void prepareSickNoteList(Person person, int year, Model model) {

        final LocalDate from = Year.of(year).atDay(1);
        final LocalDate to = from.with(lastDayOfYear());

        final List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, from, to);

        final List<SickNote> sortedSickNotes = sickNotes.stream()
            .sorted(comparing(SickNote::getStartDate).reversed())
            .toList();
        model.addAttribute("sickNotes", sortedSickNotes);

        final YearlySickDaysSummary yearlySickDaysSummary = new YearlySickDaysSummary(sickNotes, workDaysCountService, from, to);
        model.addAttribute("sickDaysOverview", yearlySickDaysSummary);
    }

    private boolean isPersonAllowedToExecuteRoleOn(Person person, Role role, Person personToShowDetails) {
        final boolean isBossOrDepartmentHeadOrSecondStageAuthority = person.hasRole(BOSS)
            || departmentService.isDepartmentHeadAllowedToManagePerson(person, personToShowDetails)
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(person, personToShowDetails);
        return person.hasRole(role) && isBossOrDepartmentHeadOrSecondStageAuthority;
    }
}
