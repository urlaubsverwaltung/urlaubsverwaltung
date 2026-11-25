package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.search.SortComparator;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNotePermissionEvaluator;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.person.PersonPageRequest.PERSON_PREFIX;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@Service
@Transactional
public class SickDaysStatisticsService {

    static final String SICK_DAYS_SORT_KEY = "sickDays";
    static final String CHILD_SICK_DAYS_SORT_KEY = "childSickDays";

    private static final Sort BY_PERSON_NAME = Sort.by(PERSON_PREFIX + ".firstName", PERSON_PREFIX + ".lastName");

    private final SickNoteService sickNoteService;
    private final DepartmentService departmentService;
    private final PersonBasedataService personBasedataService;
    private final PersonService personService;
    private final SickNotePermissionEvaluator sickNotePermissionEvaluator;

    @Autowired
    SickDaysStatisticsService(SickNoteService sickNoteService, DepartmentService departmentService, PersonBasedataService personBasedataService,
                              PersonService personService, SickNotePermissionEvaluator sickNotePermissionEvaluator) {
        this.sickNoteService = sickNoteService;
        this.departmentService = departmentService;
        this.personBasedataService = personBasedataService;
        this.personService = personService;
        this.sickNotePermissionEvaluator = sickNotePermissionEvaluator;
    }

    /**
     * Returns a page of the sick notes detailed statistics of the persons the person is allowed to access.
     * <p>
     * The page is sorted by person, or by the sick days or child sick days within the period with the sort properties
     * {@link #SICK_DAYS_SORT_KEY} and {@link #CHILD_SICK_DAYS_SORT_KEY}.
     *
     * @param person to ask for the statistics
     * @param from   a specific date
     * @param to     a specific date
     * @return page of {@link SickDaysDetailedStatistics} that the person can access
     */
    Page<SickDaysDetailedStatistics> getAll(Person person, LocalDate from, LocalDate to, PageableSearchQuery pageableSearchQuery) {

        final Pageable pageable = pageableSearchQuery.getPageable();

        final Page<Person> relevantMembersPage = getMembersForPerson(person, pageableSearchQuery);
        final List<Person> relevantMembers = relevantMembersPage.getContent();
        final List<Long> relevantPersonIds = relevantMembers.stream().map(Person::getId).toList();
        final List<SickNote> sickNotes = getSickNotes(person, relevantMembers, from, to);
        final Map<Person, List<SickNote>> sickNotesByPerson = sickNotes.stream().collect(groupingBy(SickNote::getPerson));

        final Map<PersonId, PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(relevantPersonIds);
        final Map<PersonId, List<String>> departmentsByPersonId = departmentService.getDepartmentNamesByMembers(relevantMembers);

        final List<SickDaysDetailedStatistics> statistics = relevantMembers.stream()
            .map(member -> toSickDaysDetailedStatistics(member, sickNotesByPerson, basedataByPersonId, departmentsByPersonId))
            .toList();

        if (relevantMembersPage.getPageable().isPaged()) {
            // the persons are sorted and paged already
            return new PageImpl<>(statistics, pageable, relevantMembersPage.getTotalElements());
        }

        // all persons have been fetched to sort them by their sick days, the page is cut out afterwards
        final List<SickDaysDetailedStatistics> content = sortedBySickDays(statistics, pageable.getSort(), from, to).stream()
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .toList();

        return new PageImpl<>(content, pageable, relevantMembersPage.getTotalElements());
    }

    private static SickDaysDetailedStatistics toSickDaysDetailedStatistics(Person person, Map<Person, List<SickNote>> sickNotesByPerson,
                                                                           Map<PersonId, PersonBasedata> basedataByPersonId,
                                                                           Map<PersonId, List<String>> departmentsByPersonId) {
        final PersonId personId = person.getIdAsPersonId();
        final String personnelNumber = basedataByPersonId.getOrDefault(personId, new PersonBasedata(personId, "", "")).personnelNumber();
        final List<String> departments = departmentsByPersonId.getOrDefault(personId, List.of());
        return new SickDaysDetailedStatistics(personnelNumber, person, sickNotesByPerson.getOrDefault(person, List.of()), departments);
    }

    private static List<SickDaysDetailedStatistics> sortedBySickDays(List<SickDaysDetailedStatistics> statistics, Sort sort, LocalDate from, LocalDate to) {

        final Sort.Order sickDaysOrder = sort.getOrderFor(SICK_DAYS_SORT_KEY);
        if (sickDaysOrder != null) {
            return sortedBy(statistics, statistic -> statistic.getSickDays(from, to), sickDaysOrder);
        }

        final Sort.Order childSickDaysOrder = sort.getOrderFor(CHILD_SICK_DAYS_SORT_KEY);
        if (childSickDaysOrder != null) {
            return sortedBy(statistics, statistic -> statistic.getChildSickDays(from, to), childSickDaysOrder);
        }

        return statistics;
    }

    private static List<SickDaysDetailedStatistics> sortedBy(List<SickDaysDetailedStatistics> statistics,
                                                             Function<SickDaysDetailedStatistics, SickDays> sickDays,
                                                             Sort.Order order) {

        // the days are calculated once per person, a comparator would calculate them again for every comparison
        final Function<SickDaysDetailedStatistics, BigDecimal> totalDays = sickDays.andThen(SickDaysStatisticsService::totalOf);
        final Map<SickDaysDetailedStatistics, BigDecimal> totalDaysByStatistics = statistics.stream().collect(toMap(identity(), totalDays));

        final Comparator<BigDecimal> direction = order.isAscending() ? naturalOrder() : reverseOrder();
        final Comparator<SickDaysDetailedStatistics> byDays = comparing(totalDaysByStatistics::get, direction);
        final Comparator<SickDaysDetailedStatistics> byName = new SortComparator<>(SickDaysDetailedStatistics.class, BY_PERSON_NAME);

        return statistics.stream()
            .sorted(byDays.thenComparing(byName))
            .toList();
    }

    private static BigDecimal totalOf(SickDays sickDays) {
        return sickDays.getDays().get(TOTAL.name());
    }

    private List<SickNote> getSickNotes(Person person, List<Person> members, LocalDate from, LocalDate to) {
        if (sickNotePermissionEvaluator.isAllowedToViewSickNotesOfOtherPersons(person)) {
            return sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, from, to);
        }

        return List.of();
    }

    private Page<Person> getMembersForPerson(Person person, PageableSearchQuery pageableSearchQuery) {

        final PersonPageRequest pageRequest = PersonPageRequest.ofApiPageable(pageableSearchQuery.getPageable());

        if (sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(person)) {
            return personService.getActivePersons(pageRequest, pageableSearchQuery.getQuery());
        }

        return departmentService.getManagedActiveMembersOfPerson(person, pageRequest, pageableSearchQuery.getQuery());
    }
}
