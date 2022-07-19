package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.department.web.UnknownDepartmentException;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.web.PersonDetailsBasedataDtoMapper.mapToPersonDetailsBasedataDto;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller for management of {@link Person} entities.
 */
@Controller
@RequestMapping("/web")
public class PersonDetailsViewController {

    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final DepartmentService departmentService;
    private final WorkingTimeService workingTimeService;
    private final SettingsService settingsService;
    private final PersonBasedataService personBasedataService;
    private final Clock clock;

    @Autowired
    public PersonDetailsViewController(PersonService personService, AccountService accountService,
                                       VacationDaysService vacationDaysService, DepartmentService departmentService,
                                       WorkingTimeService workingTimeService, SettingsService settingsService,
                                       PersonBasedataService personBasedataService, Clock clock) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
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

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping("/person")
    public String showPerson() {
        return "redirect:/web/person?active=true";
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping(value = "/person", params = "active")
    public String showPerson(@RequestParam(value = "active") boolean active,
                             @RequestParam(value = "department", required = false) Optional<Integer> requestedDepartmentId,
                             @RequestParam(value = "year", required = false) Optional<Integer> requestedYear,
                             Model model) throws UnknownDepartmentException {

        final Integer year = requestedYear.orElseGet(() -> Year.now(clock).getValue());

        final Person signedInUser = personService.getSignedInUser();
        final List<Person> persons = active ? getRelevantActivePersons(signedInUser) : getRelevantInactivePersons(signedInUser);


        if (requestedDepartmentId.isPresent()) {
            final Integer departmentId = requestedDepartmentId.get();
            final Department department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new UnknownDepartmentException(departmentId));

            // if department filter is active, only department members are relevant
            persons.retainAll(department.getMembers());

            model.addAttribute("department", department);
        }

        preparePersonView(signedInUser, persons, year, model);

        return "person/person_view";
    }

    private List<Person> getRelevantActivePersons(Person signedInUser) {

        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            return personService.getActivePersons();
        }

        final List<Person> membersForDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD)
            ? departmentService.getMembersForDepartmentHead(signedInUser)
            : List.of();

        final List<Person> memberForSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY)
            ? departmentService.getMembersForSecondStageAuthority(signedInUser)
            : List.of();

        return Stream.concat(memberForSecondStageAuthority.stream(), membersForDepartmentHead.stream())
            .filter(person -> !person.hasRole(INACTIVE))
            .distinct()
            .sorted(comparing(Person::getFirstName).thenComparing(Person::getLastName))
            .collect(toList());
    }

    private List<Person> getRelevantInactivePersons(Person signedInUser) {

        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            return personService.getInactivePersons();
        }

        final List<Person> membersForDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD)
            ? departmentService.getMembersForDepartmentHead(signedInUser)
            : List.of();

        final List<Person> membersForSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY)
            ? departmentService.getMembersForSecondStageAuthority(signedInUser)
            : List.of();

        return Stream.concat(membersForDepartmentHead.stream(), membersForSecondStageAuthority.stream())
            .filter(person -> person.hasRole(INACTIVE))
            .distinct()
            .sorted(comparing(Person::getFirstName).thenComparing(Person::getLastName))
            .collect(toList());
    }

    private List<Department> getRelevantDepartmentsSortedByName(Person signedInUser) {

        final Set<Department> relevantDepartments = new HashSet<>();

        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            departmentService.getAllDepartments().stream()
                .collect(toCollection(() -> relevantDepartments));
        }

        if (signedInUser.hasRole(DEPARTMENT_HEAD)) {
            departmentService.getManagedDepartmentsOfDepartmentHead(signedInUser).stream()
                .collect(toCollection(() -> relevantDepartments));
        }

        if (signedInUser.hasRole(SECOND_STAGE_AUTHORITY)) {
            departmentService.getManagedDepartmentsOfSecondStageAuthority(signedInUser).stream()
                .collect(toCollection(() -> relevantDepartments));
        }

        if (!signedInUser.isPrivileged()) {
            departmentService.getAssignedDepartmentsOfMember(signedInUser).stream()
                .collect(toCollection(() -> relevantDepartments));
        }

        return Stream.of(relevantDepartments).flatMap(Set::stream)
            .distinct()
            .sorted(comparing(Department::getName))
            .collect(toList());
    }

    private void preparePersonView(Person signedInUser, List<Person> persons, int year, Model model) {

        final LocalDate now = LocalDate.now(clock);

        final List<PersonDto> personDtos = new ArrayList<>(persons.size());
        for (Person person : persons) {
            final PersonDto.Builder personDtoBuilder = PersonDto.builder();

            final Optional<Account> account = accountService.getHolidaysAccount(year, person);
            if (account.isPresent()) {
                final Account holidaysAccount = account.get();
                final Optional<Account> accountNextYear = accountService.getHolidaysAccount(year + 1, person);
                final VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(holidaysAccount, accountNextYear);

                final boolean beforeExpiryDate = now.isBefore(holidaysAccount.getExpiryDate());
                model.addAttribute("isBeforeExpiryDate", beforeExpiryDate);

                final double remainingVacationDays = beforeExpiryDate
                    ? vacationDaysLeft.getRemainingVacationDays().doubleValue()
                    : vacationDaysLeft.getRemainingVacationDaysNotExpiring().doubleValue();

                personDtoBuilder
                    .entitlementYear(holidaysAccount.getAnnualVacationDays().doubleValue())
                    .entitlementActual(holidaysAccount.getActualVacationDays().doubleValue())
                    .entitlementRemaining(holidaysAccount.getRemainingVacationDays().doubleValue())
                    .vacationDaysLeft(vacationDaysLeft.getVacationDays().doubleValue())
                    .vacationDaysLeftRemaining(remainingVacationDays);
            }

            final String lastName = person.getFirstName() == null && person.getLastName() == null
                ? person.getUsername()
                : person.getLastName();

            personDtoBuilder
                .id(person.getId())
                .gravatarUrl(person.getGravatarURL())
                .firstName(person.getFirstName())
                .niceName(person.getNiceName())
                .lastName(lastName);

            personBasedataService.getBasedataByPersonId(person.getId())
                .ifPresent(personBasedata -> personDtoBuilder.personnelNumber(personBasedata.getPersonnelNumber()));

            final PersonDto personDto = personDtoBuilder.build();

            personDtos.add(personDto);
        }

        final boolean showPersonnelNumberColumn = personDtos.stream()
            .anyMatch(personDto -> hasText(personDto.getPersonnelNumber()));

        model.addAttribute("persons", personDtos);
        model.addAttribute("showPersonnelNumberColumn", showPersonnelNumberColumn);
        model.addAttribute("year", year);
        model.addAttribute("now", now);
        model.addAttribute("departments", getRelevantDepartmentsSortedByName(signedInUser));
    }
}
