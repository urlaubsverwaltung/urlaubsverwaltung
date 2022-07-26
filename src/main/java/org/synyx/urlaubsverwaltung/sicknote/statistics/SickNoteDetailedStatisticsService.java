package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Service for creating {@link SickNoteStatistics}.
 */
@Service
@Transactional
public class SickNoteDetailedStatisticsService {

    private final SickNoteService sickNoteService;
    private final PersonBasedataService personBasedataService;
    private final DepartmentService departmentService;

    @Autowired
    SickNoteDetailedStatisticsService(SickNoteService sickNoteService, PersonBasedataService personBasedataService,
                                      DepartmentService departmentService) {
        this.sickNoteService = sickNoteService;
        this.personBasedataService = personBasedataService;
        this.departmentService = departmentService;
    }

    List<SickNoteDetailedStatistics> getAllSicknotes(FilterPeriod period) {

        final List<SickNote> sickNotes = sickNoteService.getByPeriod(period.getStartDate(), period.getEndDate());
        final Map<Person, List<SickNote>> sickNotesByPerson = sickNotes.stream()
            .collect(groupingBy(SickNote::getPerson));

        final List<Person> personsWithSicknotes = new ArrayList<>(sickNotesByPerson.keySet());

        final List<Integer> personIds = personsWithSicknotes.stream().map(Person::getId).collect(toList());

        Map<Integer, PersonBasedata> basedataForPersons = personBasedataService.getBasedataByPersonIds(personIds);
        Map<Integer, List<String>> departmentsForPersons = departmentService.getDepartmentsByMembers(personsWithSicknotes);

        return sickNotesByPerson.entrySet().stream()
            .map(toSickNoteDetailedStatistics(basedataForPersons, departmentsForPersons))
            .collect(toList());
    }

    private Function<Map.Entry<Person, List<SickNote>>, SickNoteDetailedStatistics> toSickNoteDetailedStatistics(Map<Integer, PersonBasedata> basedataForPersons, Map<Integer, List<String>> departmentsForPersons) {
        return personListEntry ->
        {
            final Person person = personListEntry.getKey();
            final String personnelNumber = Optional.of(basedataForPersons.get(person.getId()).getPersonnelNumber()).orElse("");
            final List<String> departments = Optional.of(departmentsForPersons.get(person.getId())).orElse(emptyList());
            return new SickNoteDetailedStatistics(personnelNumber, person.getFirstName(), person.getLastName(), personListEntry.getValue(), departments);
        };
    }
}
