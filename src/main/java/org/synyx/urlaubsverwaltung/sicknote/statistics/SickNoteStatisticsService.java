package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Service for creating {@link SickNoteStatistics}.
 */
@Service
@Transactional
public class SickNoteStatisticsService {

    private final SickNoteService sickNoteService;
    private final WorkDaysCountService workDaysCountService;
    private final DepartmentService departmentService;

    @Autowired
    SickNoteStatisticsService(SickNoteService sickNoteService, WorkDaysCountService workDaysCountService, DepartmentService departmentService) {
        this.sickNoteService = sickNoteService;
        this.workDaysCountService = workDaysCountService;
        this.departmentService = departmentService;
    }

    SickNoteStatistics createStatisticsForPerson(Person person, Clock clock) {

        final Year year = Year.now(clock);

        final List<SickNote> sickNotes;
        if (person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) {
            final List<Person> members = person.hasRole(DEPARTMENT_HEAD) ? departmentService.getMembersForDepartmentHead(person) : departmentService.getMembersForSecondStageAuthority(person);
            final LocalDate firstDayOfYear = year.atDay(1);
            final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
            sickNotes = sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, firstDayOfYear, lastDayOfYear);
        } else if (person.hasRole(OFFICE) || person.hasRole(BOSS)) {
            sickNotes = sickNoteService.getAllActiveByYear(year.getValue());
        } else {
            sickNotes = List.of();
        }

        return new SickNoteStatistics(clock, sickNotes, workDaysCountService);
    }
}
