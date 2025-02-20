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
import java.util.stream.Stream;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
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

        final LocalDate firstDayOfYear = Year.now(clock).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        final List<SickNote> sickNotes = getSickNotes(person, firstDayOfYear, lastDayOfYear);
        return new SickNoteStatistics(clock, sickNotes, workDaysCountService);
    }

    private List<SickNote> getSickNotes(Person person, LocalDate from, LocalDate to) {

        if (person.hasRole(OFFICE) || (person.hasRole(BOSS) && person.hasRole(SICK_NOTE_VIEW))) {
            return sickNoteService.getAllActiveByPeriod(from, to);
        }

        final List<SickNote> sickNotes;
        if ((person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) && person.hasRole(SICK_NOTE_VIEW)) {
            final List<Person> members = getMembersForPerson(person);
            sickNotes = sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, from, to);
        } else {
            sickNotes = List.of();
        }

        return sickNotes;
    }

    private List<Person> getMembersForPerson(Person person) {
        final List<Person> membersDH = person.hasRole(DEPARTMENT_HEAD) ? departmentService.getMembersForDepartmentHead(person) : List.of();
        final List<Person> membersSSA = person.hasRole(SECOND_STAGE_AUTHORITY) ? departmentService.getMembersForSecondStageAuthority(person) : List.of();
        return Stream.concat(membersDH.stream(), membersSSA.stream())
            .distinct()
            .toList();
    }
}
