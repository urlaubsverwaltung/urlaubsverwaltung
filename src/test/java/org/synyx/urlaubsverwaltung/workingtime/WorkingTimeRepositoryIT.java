package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

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

        final WorkingTime expectedWorkingTime1 = getWorkingTime(person, start.minusDays(1), List.of(1));
        sut.save(expectedWorkingTime1);

        final WorkingTime expectedWorkingTime2 = getWorkingTime(person, start.plusDays(1), List.of(2));
        sut.save(expectedWorkingTime2);

        final WorkingTime expectedWorkingTime3 = getWorkingTime(person, start.plusDays(2), List.of(3));
        sut.save(expectedWorkingTime3);

        final WorkingTime expectedWorkingTime4 = getWorkingTime(person, end.plusDays(1), List.of(4));
        sut.save(expectedWorkingTime4);

        final List<WorkingTime> workingTimes = sut.findByPersonInAndValidFromForDateInterval(persons, start, end);

        assertThat(workingTimes).hasSize(3)
            .containsExactlyInAnyOrder(expectedWorkingTime1, expectedWorkingTime2, expectedWorkingTime3);
    }

    private WorkingTime getWorkingTime(Person person, LocalDate start, List<Integer> workingDays) {

        final WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(workingDays, FULL);
        workingTime.setValidFrom(start);
        workingTime.setPerson(person);
        return workingTime;
    }
}
