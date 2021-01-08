package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.of;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class OvertimeRepositoryIT extends TestContainersBase {

    @Autowired
    private PersonService personService;

    @Autowired
    private OvertimeRepository sut;

    @Test
    void ensureCanPersistOvertime() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.save(person);

        final LocalDate now = LocalDate.now(UTC);
        final Overtime overtime = new Overtime(savedPerson, now, now.plusDays(2), Duration.ofHours(1));
        assertThat(overtime.getId()).isNull();

        sut.save(overtime);
        assertThat(overtime.getId()).isNotNull();
    }

    @Test
    void ensureCountsTotalHoursCorrectly() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final Person otherPerson = new Person("freddy", "Gwin", "freddy", "gwin@example.org");
        final Person savedOtherPerson = personService.save(otherPerson);

        final LocalDate now = LocalDate.now(UTC);

        // Overtime for person
        sut.save(new Overtime(savedPerson, now, now.plusDays(2), Duration.ofHours(3)));
        sut.save(new Overtime(savedPerson, now.plusDays(5), now.plusDays(10), Duration.ofMinutes(30)));
        sut.save(new Overtime(savedPerson, now.minusDays(8), now.minusDays(4), Duration.ofHours(-1)));

        // Overtime for other person
        sut.save(new Overtime(savedOtherPerson, now.plusDays(5), now.plusDays(10), Duration.ofHours(5)));

        final Double totalHours = sut.calculateTotalHoursForPerson(person);
        assertThat(totalHours).isEqualTo(Duration.ofMinutes(150).toMinutes() / 60.0);
    }

    @Test
    void ensureReturnsNullAsTotalOvertimeIfPersonHasNoOvertimeRecords() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        personService.save(person);

        final Double totalHours = sut.calculateTotalHoursForPerson(person);
        assertThat(totalHours).isNull();
    }

    @Test
    void ensureReturnsAllRecordsWithStartOrEndDateInTheGivenYear() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.save(person);

        // records for 2015
        sut.save(new Overtime(savedPerson, of(2014, 12, 30), of(2015, 1, 3), Duration.ofHours(1)));
        sut.save(new Overtime(savedPerson, of(2015, 10, 5), of(2015, 10, 20), Duration.ofHours(2)));
        sut.save(new Overtime(savedPerson, of(2015, 12, 28), of(2016, 1, 6), Duration.ofHours(3)));

        // record for 2014
        sut.save(new Overtime(savedPerson, of(2014, 12, 5), of(2014, 12, 31), Duration.ofHours(4)));

        final List<Overtime> overtimes = sut.findByPersonAndPeriod(savedPerson, of(2015, 1, 1), of(2015, 12, 31));
        assertThat(overtimes).hasSize(3);
        assertThat(overtimes.get(0).getStartDate()).isEqualTo(of(2015, 12, 28));
        assertThat(overtimes.get(1).getStartDate()).isEqualTo(of(2015, 10, 5));
        assertThat(overtimes.get(2).getStartDate()).isEqualTo(of(2014, 12, 30));
        assertThat(overtimes.get(0).getDuration()).isEqualTo(Duration.ofHours(3));
        assertThat(overtimes.get(1).getDuration()).isEqualTo(Duration.ofHours(2));
        assertThat(overtimes.get(2).getDuration()).isEqualTo(Duration.ofHours(1));
    }
}
