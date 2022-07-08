package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * Service for creating {@link SickNoteStatistics}.
 */
@Service
@Transactional
public class SickNoteStatisticsService {

    private final SickNoteService sickNoteService;
    private final WorkDaysCountService workDaysCountService;

    @Autowired
    SickNoteStatisticsService(SickNoteService sickNoteService, WorkDaysCountService workDaysCountService) {
        this.sickNoteService = sickNoteService;
        this.workDaysCountService = workDaysCountService;
    }

    SickNoteStatistics createStatistics(Clock clock) {
        return new SickNoteStatistics(clock, sickNoteService, workDaysCountService);
    }

    Map<Person, List<SickNote>> getAllSicknotes(FilterPeriod period) {

        final List<SickNote> sickNotes = sickNoteService.getByPeriod(period.getStartDate(), period.getEndDate());
        return sickNotes.stream().collect(groupingBy(SickNote::getPerson));
    }
}
