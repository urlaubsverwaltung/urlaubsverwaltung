package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Service for creating {@link SickNoteStatistics}.
 */
@Service
@Transactional
public class SickNoteDetailedStatisticsService {

    private final SickNoteService sickNoteService;
    private final WorkDaysCountService workDaysCountService;
    private final PersonBasedataService personBasedataService;

    @Autowired
    SickNoteDetailedStatisticsService(SickNoteService sickNoteService, WorkDaysCountService workDaysCountService,
                                      PersonBasedataService personBasedataService) {
        this.sickNoteService = sickNoteService;
        this.workDaysCountService = workDaysCountService;
        this.personBasedataService = personBasedataService;
    }

    SickNoteStatistics createStatistics(Clock clock) {
        return new SickNoteStatistics(clock, sickNoteService, workDaysCountService);
    }

    List<SickNoteDetailedStatistics> getAllSicknotes(FilterPeriod period) {

        final List<SickNote> sickNotes = sickNoteService.getByPeriod(period.getStartDate(), period.getEndDate());
        final Map<Person, List<SickNote>> sickNotesByPerson = sickNotes.stream()
            .collect(groupingBy(SickNote::getPerson));

        final List<Integer> personIds = sickNotesByPerson.keySet().stream().map(Person::getId).collect(toList());

        Map<Integer, PersonBasedata> basedataForPersons = personBasedataService.getBasedataByPersonIds(personIds);

        return sickNotesByPerson.entrySet().stream()
            .map(toSickNoteDetailedStatistics(basedataForPersons))
            .collect(toList());
    }

    private Function<Map.Entry<Person, List<SickNote>>, SickNoteDetailedStatistics> toSickNoteDetailedStatistics(Map<Integer, PersonBasedata> basedataForPersons) {
        return personListEntry ->
        {
            final Person person = personListEntry.getKey();
            final String personnelNumber = Optional.of(basedataForPersons.get(person.getId()).getPersonnelNumber()).orElse("");
            return new SickNoteDetailedStatistics(personnelNumber, person.getFirstName(), person.getLastName(), personListEntry.getValue());
        };
    }
}
