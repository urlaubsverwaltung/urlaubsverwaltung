package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.search.SortComparator;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptgroupDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptionDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlSelectDto;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

@Controller
@RequestMapping("/web")
public class PersonsViewController {

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final DepartmentService departmentService;
    private final PersonBasedataService personBasedataService;
    private final Clock clock;

    @Autowired
    public PersonsViewController(PersonService personService, AccountService accountService,
                                 VacationDaysService vacationDaysService, DepartmentService departmentService,
                                 PersonBasedataService personBasedataService, Clock clock) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.departmentService = departmentService;
        this.personBasedataService = personBasedataService;
        this.clock = clock;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping("/person")
    public String showPerson(@RequestParam(value = "active", required = false, defaultValue = "true") boolean active,
                             @RequestParam(value = "department", required = false) Optional<Integer> requestedDepartmentId,
                             @RequestParam(value = "year", required = false) Optional<Integer> requestedYear,
                             @RequestParam(value = "query", required = false, defaultValue = "") String query,
                             @SortDefault.SortDefaults({
                                 @SortDefault(sort = "person.firstName", direction = Sort.Direction.ASC)
                             })
                             Pageable pageable,
                             @RequestHeader(name = "Turbo-Frame", required = false) String turboFrame,
                             Model model) throws UnknownDepartmentException {

        final int currentYear = Year.now(clock).getValue();
        final Integer selectedYear = requestedYear.orElse(currentYear);

        final Person signedInUser = personService.getSignedInUser();

        Sort personSort = Sort.unsorted();
        Sort accountSort = Sort.unsorted();
        for (Sort.Order order : pageable.getSort()) {
            final String propertyWithPrefix = order.getProperty();
            if (propertyWithPrefix.startsWith("person.")) {
                final String property = propertyWithPrefix.replace("person.", "");
                personSort = personSort.and(Sort.by(order.getDirection(), property));
            } else if (propertyWithPrefix.startsWith("account.")) {
                final String property = propertyWithPrefix.replace("account.", "");
                accountSort = accountSort.and(Sort.by(order.getDirection(), property));
            }
        }
        final Pageable personPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), personSort);

        final PageableSearchQuery personPageableSearchQuery = new PageableSearchQuery(personPageable, query);
        final Page<Person> personPage;

        if (requestedDepartmentId.isPresent()) {
            final Integer departmentId = requestedDepartmentId.get();
            final Department department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new UnknownDepartmentException(departmentId));

            model.addAttribute("department", department);

            personPage = active
                ? departmentService.getManagedMembersOfPersonAndDepartment(signedInUser, departmentId, personPageableSearchQuery)
                : departmentService.getManagedInactiveMembersOfPersonAndDepartment(signedInUser, departmentId, personPageableSearchQuery);


        } else {
            personPage = active
                ? getRelevantActivePersons(signedInUser, personPageableSearchQuery)
                : getRelevantInactivePersons(signedInUser, personPageableSearchQuery);
        }

        preparePersonView(signedInUser, personPage, personSort, accountSort, pageable.getSort(), selectedYear, model);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("active", active);
        model.addAttribute("query", query);
        model.addAttribute("turboFrameRequested", StringUtils.hasText(turboFrame));

        if (turboFrame == null || turboFrame.isBlank()) {
            return "thymeleaf/person/persons";
        } else {
            return "thymeleaf/person/persons::#" + turboFrame;
        }
    }

    private Page<Person> getRelevantActivePersons(Person signedInUser, PageableSearchQuery personPageableSearchQuery) {
        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            return personService.getActivePersons(personPageableSearchQuery);
        } else {
            return departmentService.getManagedMembersOfPerson(signedInUser, personPageableSearchQuery);
        }
    }

    private Page<Person> getRelevantInactivePersons(Person signedInUser, PageableSearchQuery personPageableSearchQuery) {
        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            return personService.getInactivePersons(personPageableSearchQuery);
        }
        return departmentService.getManagedInactiveMembersOfPerson(signedInUser, personPageableSearchQuery);
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

    private void preparePersonView(Person signedInUser, Page<Person> personPage, Sort originalPersonSort, Sort originalAccountSort, Sort originalSort, int year, Model model) {

        final LocalDate now = LocalDate.now(clock);

        final List<PersonDto> personDtos = new ArrayList<>(personPage.getContent().size());
        for (Person person : personPage) {
            final PersonDto.Builder personDtoBuilder = PersonDto.builder();

            final Optional<Account> account = accountService.getHolidaysAccount(year, person);
            if (account.isPresent()) {
                final Account holidaysAccount = account.get();
                final Optional<Account> accountNextYear = accountService.getHolidaysAccount(year + 1, person);
                final VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(holidaysAccount, accountNextYear);

                final boolean beforeExpiryDate = now.isBefore(holidaysAccount.getExpiryDate());
                final double remainingVacationDays = holidaysAccount.isDoRemainingVacationDaysExpire() || beforeExpiryDate
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

        if (!originalAccountSort.equals(Sort.unsorted())) {
            final Comparator<PersonDto> accountComparator = new SortComparator<>(PersonDto.class, originalAccountSort);
            personDtos.sort(accountComparator.thenComparing(PersonDto::getNiceName));
        }

        final boolean showPersonnelNumberColumn = personDtos.stream()
            .anyMatch(personDto -> hasText(personDto.getPersonnelNumber()));

        final PageImpl<PersonDto> personDtoPage = new PageImpl<>(personDtos, personPage.getPageable(), personPage.getTotalElements());
        model.addAttribute("personPage", personDtoPage);

        final HtmlSelectDto htmlSelectDto = htmlSelectDto(originalPersonSort, originalAccountSort);
        model.addAttribute("sortSelect", htmlSelectDto);

        final List<Integer> pageNumbers = IntStream.rangeClosed(1, personDtoPage.getTotalPages()).boxed().collect(toList());
        model.addAttribute("personPageNumbers", pageNumbers);

        model.addAttribute("showPersonnelNumberColumn", showPersonnelNumberColumn);
        model.addAttribute("now", now);
        model.addAttribute("departments", getRelevantDepartmentsSortedByName(signedInUser));
        model.addAttribute("sortQuery", originalSort.stream().map(order -> order.getProperty() + "," + order.getDirection()).collect(toList()).stream().reduce((s, s2) -> s + "&" + s2).orElse(""));
    }

    private static HtmlSelectDto htmlSelectDto(Sort originalPersonSort, Sort originalAccountSort) {

        final List<HtmlOptionDto> personOptions = htmlOptionDtos("person", List.of("firstName", "lastName"), originalPersonSort);
        final HtmlOptgroupDto personOptgroup = new HtmlOptgroupDto("persons.sort.optgroup.person.label", personOptions);

        final List<HtmlOptionDto> urlaubOptions = htmlOptionDtos("account", List.of("entitlementYear", "entitlementActual", "vacationDaysLeft"), originalAccountSort);
        final HtmlOptgroupDto urlaubOptgroup = new HtmlOptgroupDto("persons.sort.optgroup.urlaub.label", urlaubOptions);

        final List<HtmlOptionDto> resturlaubOptions = htmlOptionDtos("account", List.of("entitlementRemaining", "vacationDaysLeftRemaining"), originalAccountSort);
        final HtmlOptgroupDto resturlaubOptgroup = new HtmlOptgroupDto("persons.sort.optgroup.resturlaub.label", resturlaubOptions);

        return new HtmlSelectDto(List.of(personOptgroup, urlaubOptgroup, resturlaubOptgroup));
    }

    private static List<HtmlOptionDto> htmlOptionDtos(String propertyPrefix, List<String> properties, Sort sort) {
        final List<HtmlOptionDto> options = new ArrayList<>();

        for (String property : properties) {
            final Sort.Order order = sort.getOrderFor(property);
            options.addAll(htmlOptionDto(propertyPrefix, property, order));
        }

        return options;
    }

    private static List<HtmlOptionDto> htmlOptionDto(String propertyPrefix, String property, Sort.Order order) {
        return List.of(
            new HtmlOptionDto(String.format("persons.sort.%s.asc", property), propertyPrefix + "." + property + ",asc", order != null && order.isAscending()),
            new HtmlOptionDto(String.format("persons.sort.%s.desc", property), propertyPrefix + "." + property + ",desc", order != null && order.isDescending())
        );
    }
}
