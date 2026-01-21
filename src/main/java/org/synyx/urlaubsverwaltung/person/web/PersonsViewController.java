package org.synyx.urlaubsverwaltung.person.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.department.web.UnknownDepartmentException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonPageable;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.PersonSortProperty;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.SortComparator;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptgroupDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptionDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlSelectDto;
import org.synyx.urlaubsverwaltung.web.html.PaginationDto;
import org.synyx.urlaubsverwaltung.web.html.PaginationPageLinkBuilder.QueryParam;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;
import static org.synyx.urlaubsverwaltung.web.html.PaginationPageLinkBuilder.buildPageLinkPrefix;

@Controller
@RequestMapping("/web")
public class PersonsViewController implements HasLaunchpad {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final DepartmentService departmentService;
    private final PersonBasedataService personBasedataService;
    private final DataWebProperties dataWebProperties;
    private final Clock clock;

    @Autowired
    public PersonsViewController(
        PersonService personService, AccountService accountService,
        VacationDaysService vacationDaysService, DepartmentService departmentService,
        PersonBasedataService personBasedataService, DataWebProperties dataWebProperties, Clock clock
    ) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.departmentService = departmentService;
        this.personBasedataService = personBasedataService;
        this.dataWebProperties = dataWebProperties;
        this.clock = clock;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping("/person")
    public String showPerson(
        @RequestParam(value = "active", required = false, defaultValue = "true") boolean active,
        @RequestParam(value = "department", required = false) Optional<Long> requestedDepartmentId,
        @RequestParam(value = "year", required = false) Optional<Integer> requestedYear,
        @RequestParam(value = "query", required = false, defaultValue = "") String query,
        @SortDefault(sort = PersonPageRequest.DEFAULT_PERSON_SORT_KEY, direction = Sort.Direction.ASC) Pageable pageable,
        Model model
    ) throws UnknownDepartmentException {
        final int currentYear = Year.now(clock).getValue();
        final Integer selectedYear = requestedYear.orElse(currentYear);
        final LocalDate now = LocalDate.now(clock);

        final Person signedInUser = personService.getSignedInUser();

        final PersonPageRequest personPageRequest = PersonPageRequest.ofApiPageable(pageable);

        // TODO typed account sort/pageRequest
        Sort accountSort = Sort.unsorted();
        for (Sort.Order order : pageable.getSort()) {
            final String propertyWithPrefix = order.getProperty();
            final String property = propertyWithPrefix.replace("account.", "");
            accountSort = accountSort.and(Sort.by(order.getDirection(), property));
        }

        Page<Person> personPage = null;

        final boolean departmentPresent = requestedDepartmentId.isPresent();
        Department department = null;
        if (departmentPresent) {
            final Long departmentId = requestedDepartmentId.get();
            department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new UnknownDepartmentException(departmentId));

            model.addAttribute("isDepartmentPresent", departmentPresent);

            if (departmentService.isPersonAllowedToManageDepartment(signedInUser, department)) {
                model.addAttribute("department", department);
                personPage = active
                    ? departmentService.getManagedMembersOfPersonAndDepartment(signedInUser, departmentId, personPageRequest, query)
                    : departmentService.getManagedInactiveMembersOfPersonAndDepartment(signedInUser, departmentId, personPageRequest, query);
            }
        }

        if (personPage == null) {
            personPage = active
                ? getRelevantActivePersons(signedInUser, personPageRequest, query)
                : getRelevantInactivePersons(signedInUser, personPageRequest, query);
        }

        final Page<PersonDto> personDtoPage = personPage(personPage, accountSort, selectedYear, now, departmentPresent, department);
        final boolean showPersonnelNumberColumn = personDtoPage.getContent().stream()
            .anyMatch(personDto -> hasText(personDto.getPersonnelNumber()));

        final String sortQuery = pageable.getSort().stream().map(order -> order.getProperty() + "," + order.getDirection()).collect(joining("&"));

        final List<QueryParam> paginationLinkParameters = new ArrayList<>();
        paginationLinkParameters.add(new QueryParam("active", String.valueOf(active)));
        paginationLinkParameters.add(new QueryParam("query", query));
        requestedDepartmentId.ifPresent(departmentId -> paginationLinkParameters.add(new QueryParam("department", String.valueOf(departmentId))));
        requestedYear.ifPresent(year -> paginationLinkParameters.add(new QueryParam("year", String.valueOf(year))));

        final String pageLinkPrefix = buildPageLinkPrefix(pageable, paginationLinkParameters);
        final PaginationDto<PersonDto> personsPagination = new PaginationDto<>(personDtoPage, pageLinkPrefix, dataWebProperties.getPageable());
        model.addAttribute("personsPagination", personsPagination);
        model.addAttribute("paginationPageNumbers", IntStream.range(0, personDtoPage.getTotalPages()).boxed().toList());

        final HtmlSelectDto htmlSelectDto = htmlSelectDto(personPageRequest.getSort(), accountSort);
        model.addAttribute("sortSelect", htmlSelectDto);

        model.addAttribute("showPersonnelNumberColumn", showPersonnelNumberColumn);
        model.addAttribute("now", now);
        model.addAttribute("departments", getRelevantDepartmentsSortedByName(signedInUser));
        model.addAttribute("sortQuery", sortQuery);

        model.addAttribute("currentYear", currentYear);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("active", active);
        model.addAttribute("query", query);

        return "person/persons";
    }

    private Page<Person> getRelevantActivePersons(Person signedInUser, PersonPageRequest personPageRequest, String query) {
        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            return personService.getActivePersons(personPageRequest, query);
        } else {
            return departmentService.getManagedMembersOfPerson(signedInUser, personPageRequest, query);
        }
    }

    private Page<Person> getRelevantInactivePersons(Person signedInUser, PersonPageable personPageable, String query) {
        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            return personService.getInactivePersons(personPageable, query);
        }
        return departmentService.getManagedInactiveMembersOfPerson(signedInUser, personPageable, query);
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
            .toList();
    }

    private Page<PersonDto> personPage(Page<Person> personPage, Sort originalAccountSort, int year, LocalDate now, boolean departmentSelected, Department department) {

        final List<PersonDto> personDtos = new ArrayList<>(personPage.getContent().size());
        final List<Person> persons = personPage.stream().toList();

        final List<Account> holidaysAccounts = accountService.getHolidaysAccount(year, persons);
        final List<Account> holidaysAccountsNextYear = accountService.getHolidaysAccount(year + 1, persons);

        final Map<Account, HolidayAccountVacationDays> accountHolidayAccountVacationDaysMap = vacationDaysService.getVacationDaysLeft(holidaysAccounts, Year.of(year), holidaysAccountsNextYear);

        for (Person person : personPage) {
            final PersonDto.Builder personDtoBuilder = PersonDto.builder();

            final Optional<Account> maybeAccount = accountHolidayAccountVacationDaysMap.keySet().stream()
                .filter(account -> account.getPerson().equals(person))
                .findFirst();

            if (maybeAccount.isPresent()) {

                final Account account = maybeAccount.get();
                final HolidayAccountVacationDays holidayAccountVacationDays = accountHolidayAccountVacationDaysMap.get(account);

                final boolean doRemainingVacationDaysExpire = account.doRemainingVacationDaysExpire();
                final LocalDate expiryDate = account.getExpiryDate();

                final VacationDaysLeft vacationDaysLeft = holidayAccountVacationDays.vacationDaysYear();
                final double remainingVacationDays = vacationDaysLeft.getRemainingVacationDaysLeft(now, doRemainingVacationDaysExpire, expiryDate).doubleValue();

                personDtoBuilder
                    .entitlementYear(account.getAnnualVacationDays().doubleValue())
                    .entitlementActual(account.getActualVacationDays().doubleValue())
                    .entitlementRemaining(account.getRemainingVacationDays().doubleValue())
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
                .initials(person.getInitials())
                .lastName(lastName);

            personBasedataService.getBasedataByPersonId(person.getId())
                .ifPresent(personBasedata -> personDtoBuilder.personnelNumber(personBasedata.personnelNumber()));

            if (departmentSelected) {
                personDtoBuilder.isDepartmentHead(department.getDepartmentHeads().contains(person));
                personDtoBuilder.isSecondStageAuthority(department.getSecondStageAuthorities().contains(person));
            }

            final PersonDto personDto = personDtoBuilder.build();

            personDtos.add(personDto);
        }

        if (!originalAccountSort.equals(Sort.unsorted())) {
            final Comparator<PersonDto> accountComparator = new SortComparator<>(PersonDto.class, originalAccountSort);
            personDtos.sort(accountComparator.thenComparing(PersonDto::getNiceName));
        }

        return new PageImpl<>(personDtos, personPage.getPageable(), personPage.getTotalElements());
    }

    private static HtmlSelectDto htmlSelectDto(Sort personSort, Sort accountSort) {

        final List<String> sortablePersonProperties = List.of(
            PersonSortProperty.FIRST_NAME.key(),
            PersonSortProperty.LAST_NAME.key()
        );

        final List<HtmlOptionDto> personOptions = htmlOptionDtos(PersonPageRequest.PERSON_PREFIX, sortablePersonProperties, personSort);
        final HtmlOptgroupDto personOptgroup = new HtmlOptgroupDto("persons.sort.optgroup.person.label", personOptions);

        final List<HtmlOptionDto> urlaubOptions = htmlOptionDtos("account", List.of("entitlementYear", "entitlementActual", "vacationDaysLeft"), accountSort);
        final HtmlOptgroupDto urlaubOptgroup = new HtmlOptgroupDto("persons.sort.optgroup.urlaub.label", urlaubOptions);

        final List<HtmlOptionDto> resturlaubOptions = htmlOptionDtos("account", List.of("entitlementRemaining", "vacationDaysLeftRemaining"), accountSort);
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
