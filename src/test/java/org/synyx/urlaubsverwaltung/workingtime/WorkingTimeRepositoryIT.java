package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WorkingTimeRepositoryIT extends TestContainersBase {

    @Autowired
    private WorkingTimeRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void ensureFindByPersonInAndValidFromForDateInterval() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        personService.save(person);
        final List<Person> persons = List.of(person);

        final LocalDate start = LocalDate.now();
        final LocalDate end = start.plusMonths(1);

        final WorkingTimeEntity expectedWorkingTime1 = getWorkingTime(person, start.minusDays(1), List.of(MONDAY));
        sut.save(expectedWorkingTime1);

        final WorkingTimeEntity expectedWorkingTime2 = getWorkingTime(person, start.plusDays(1), List.of(TUESDAY));
        sut.save(expectedWorkingTime2);

        final WorkingTimeEntity expectedWorkingTime3 = getWorkingTime(person, start.plusDays(2), List.of(WEDNESDAY));
        sut.save(expectedWorkingTime3);

        final WorkingTimeEntity expectedWorkingTime4 = getWorkingTime(person, end.plusDays(1), List.of(THURSDAY));
        sut.save(expectedWorkingTime4);

        final List<WorkingTimeEntity> workingTimes = sut.findByPersonInAndValidFromForDateInterval(persons, start, end);

        assertThat(workingTimes).hasSize(3)
            .containsExactlyInAnyOrder(expectedWorkingTime1, expectedWorkingTime2, expectedWorkingTime3);
    }

    private WorkingTimeEntity getWorkingTime(Person person, LocalDate start, List<DayOfWeek> workDays) {

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setValidFrom(start);
        workingTimeEntity.setPerson(person);

        for (DayOfWeek dayOfWeek : workDays) {
            switch (dayOfWeek) {
                case MONDAY:
                    workingTimeEntity.setMonday(DayLength.FULL);
                    break;
                case TUESDAY:
                    workingTimeEntity.setTuesday(DayLength.FULL);
                    break;
                case WEDNESDAY:
                    workingTimeEntity.setWednesday(DayLength.FULL);
                    break;
                case THURSDAY:
                    workingTimeEntity.setThursday(DayLength.FULL);
                    break;
                case FRIDAY:
                    workingTimeEntity.setFriday(DayLength.FULL);
                    break;
                case SATURDAY:
                    workingTimeEntity.setSaturday(DayLength.FULL);
                    break;
                case SUNDAY:
                    workingTimeEntity.setSunday(DayLength.FULL);
                    break;
            }
        }

        return workingTimeEntity;
    }
}
