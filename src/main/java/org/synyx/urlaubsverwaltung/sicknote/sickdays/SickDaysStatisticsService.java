package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.search.SortComparator;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@Service
@Transactional
public class SickDaysStatisticsService {

    private final SickNoteService sickNoteService;
    private final DepartmentService departmentService;
    private final PersonBasedataService personBasedataService;
    private final PersonService personService;

    @Autowired
    SickDaysStatisticsService(SickNoteService sickNoteService, DepartmentService departmentService, PersonBasedataService personBasedataService, PersonService personService) {
        this.sickNoteService = sickNoteService;
        this.departmentService = departmentService;
        this.personBasedataService = personBasedataService;
        this.personService = personService;
    }

    /**
     * Returns a list of all sick notes detailed statistics that the person is allowed to access.
     *
     * @param person to ask for the statistics
     * @param from   a specific date
     * @param to     a specific date
     * @return list of all {@link SickDaysDetailedStatistics} that the person can access
     */
    Page<SickDaysDetailedStatistics> getAll(Person person, LocalDate from, LocalDate to, PageableSearchQuery pageableSearchQuery) {

        final Pageable pageable = pageableSearchQuery.getPageable();

        final Page<Person> relevantMembersPage = getMembersForPerson(person, pageableSearchQuery);
        final List<Person> relevantMembers = relevantMembersPage.getContent();
        final List<Long> relevantPersonIds = relevantMembers.stream().map(Person::getId).toList();
        final List<SickNote> sickNotes = getSickNotes(person, relevantMembers, from, to);

        final Map<Person, List<SickNote>> sickNotesByPerson = sickNotes.stream().collect(groupingBy(SickNote::getPerson));
        for (Person member : relevantMembers) {
            sickNotesByPerson.putIfAbsent(member, List.of());
        }

        final Map<PersonId, PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(relevantPersonIds);
        final Map<PersonId, List<String>> departmentsByPersonId = departmentService.getDepartmentNamesByMembers(relevantMembers);

        Stream<SickDaysDetailedStatistics> statisticsStream = sickNotesByPerson.entrySet()
            .stream()
            .map(toSickNoteDetailedStatistics(basedataByPersonId, departmentsByPersonId));

        if (relevantMembersPage.getPageable().isUnpaged()) {
            // we don't have to restrict the statistics if persons page is paged and or sorted already.
            // otherwise we have fetched ALL persons -> therefore skip and limit statistics content.
            statisticsStream = statisticsStream
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize());
        }

        final List<SickDaysDetailedStatistics> content = statisticsStream
            .sorted(new SortComparator<>(SickDaysDetailedStatistics.class, pageable.getSort()))
            .toList();

        return new PageImpl<>(content, pageable, relevantMembersPage.getTotalElements());
    }

    private Function<Map.Entry<Person, List<SickNote>>, SickDaysDetailedStatistics> toSickNoteDetailedStatistics(Map<PersonId, PersonBasedata> basedataForPersons, Map<PersonId, List<String>> departmentsForPersons) {
        return personListEntry ->
        {
            final Person person = personListEntry.getKey();
            final PersonId personId = new PersonId(person.getId());
            final String personnelNumber = basedataForPersons.getOrDefault(personId, new PersonBasedata(personId, "", "")).personnelNumber();
            final List<String> departments = departmentsForPersons.getOrDefault(personId, List.of());
            return new SickDaysDetailedStatistics(personnelNumber, person, personListEntry.getValue(), departments);
        };
    }

    private List<SickNote> getSickNotes(Person person, List<Person> members, LocalDate from, LocalDate to) {
        if (person.hasRole(OFFICE) || (person.hasRole(BOSS) || person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) && person.hasRole(SICK_NOTE_VIEW)) {
            return sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, from, to);
        }

        return List.of();
    }

    private Page<Person> getMembersForPerson(Person person, PageableSearchQuery pageableSearchQuery) {
        final Pageable pageable = pageableSearchQuery.getPageable();
        final boolean sortByPerson = isSortByPersonAttribute(pageable);

        if (person.hasRole(OFFICE) || person.hasRole(BOSS) && person.hasRole(SICK_NOTE_VIEW)) {
            final PageableSearchQuery query = sortByPerson
                ? new PageableSearchQuery(mapToPersonPageRequest(pageable), pageableSearchQuery.getQuery())
                : new PageableSearchQuery(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), pageableSearchQuery.getQuery());

            return personService.getActivePersons(query);
        }

        final PageableSearchQuery query = new PageableSearchQuery(sortByPerson ? mapToPersonPageRequest(pageable) : Pageable.unpaged(), pageableSearchQuery.getQuery());
        return departmentService.getManagedMembersOfPerson(person, query);
    }

    private boolean isSortByPersonAttribute(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!order.getProperty().startsWith("person.")) {
                return false;
            }
        }
        return true;
    }

    private PageRequest mapToPersonPageRequest(Pageable statisticsPageRequest) {
        Sort personSort = Sort.unsorted();

        for (Sort.Order order : statisticsPageRequest.getSort()) {
            if (order.getProperty().startsWith("person.")) {
                personSort = personSort.and(Sort.by(order.getDirection(), order.getProperty().replace("person.", "")));
            }
        }

        return PageRequest.of(statisticsPageRequest.getPageNumber(), statisticsPageRequest.getPageSize(), personSort);
    }
}
