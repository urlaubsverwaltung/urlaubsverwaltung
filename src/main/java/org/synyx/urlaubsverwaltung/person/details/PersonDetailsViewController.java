package org.synyx.urlaubsverwaltung.person.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

import static java.lang.String.format;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.details.PersonDetailsBasedataDtoMapper.mapToPersonDetailsBasedataDto;

/**
 * Controller for management of {@link Person} entities.
 */
@Controller
@RequestMapping("/web")
public class PersonDetailsViewController {

    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final AccountService accountService;
    private final DepartmentService departmentService;
    private final WorkingTimeService workingTimeService;
    private final SettingsService settingsService;
    private final PersonBasedataService personBasedataService;
    private final Clock clock;

    @Autowired
    PersonDetailsViewController(PersonService personService, AccountService accountService,
                                DepartmentService departmentService, WorkingTimeService workingTimeService,
                                SettingsService settingsService, PersonBasedataService personBasedataService, Clock clock) {
        this.personService = personService;
        this.accountService = accountService;
        this.departmentService = departmentService;
        this.workingTimeService = workingTimeService;
        this.settingsService = settingsService;
        this.personBasedataService = personBasedataService;
        this.clock = clock;
    }

    @GetMapping("/person/{personId}")
    public String showPersonInformation(@PathVariable("personId") Integer personId,
                                        @RequestParam(value = "year", required = false) Optional<Integer> requestedYear,
                                        Model model) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        final Person signedInUser = personService.getSignedInUser();

        if (!departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to access data of user '%s'", signedInUser.getId(), person.getId()));
        }

        final Integer year = requestedYear.orElseGet(() -> Year.now(clock).getValue());
        model.addAttribute("year", year);

        model.addAttribute(PERSON_ATTRIBUTE, person);

        final Optional<PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(person.getId());
        if (basedataByPersonId.isPresent()) {
            final PersonDetailsBasedataDto personDetailsBasedataDto = mapToPersonDetailsBasedataDto(basedataByPersonId.get());
            model.addAttribute("personBasedata", personDetailsBasedataDto);
        }

        model.addAttribute("departments", departmentService.getAssignedDepartmentsOfMember(person));
        model.addAttribute("departmentHeadOfDepartments", departmentService.getManagedDepartmentsOfDepartmentHead(person));
        model.addAttribute("secondStageAuthorityOfDepartments", departmentService.getManagedDepartmentsOfSecondStageAuthority(person));

        final Optional<WorkingTime> maybeWorkingTime = workingTimeService.getWorkingTime(person, LocalDate.now(clock));
        model.addAttribute("workingTime", maybeWorkingTime.orElse(null));
        model.addAttribute("federalState", maybeWorkingTime.map(WorkingTime::getFederalState)
            .orElseGet(() -> settingsService.getSettings().getWorkingTimeSettings().getFederalState()));

        model.addAttribute("canEditBasedata", signedInUser.hasRole(OFFICE));
        model.addAttribute("canEditPermissions", signedInUser.hasRole(OFFICE));
        model.addAttribute("canEditDepartments", signedInUser.hasRole(OFFICE));
        model.addAttribute("canEditAccounts", signedInUser.hasRole(OFFICE));
        model.addAttribute("canEditWorkingtime", signedInUser.hasRole(OFFICE));

        final Optional<Account> maybeAccount = accountService.getHolidaysAccount(year, person);
        maybeAccount.ifPresent(account -> model.addAttribute("account", account));

        return "person/person_detail";
    }
}
