package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.math.RoundingMode.UNNECESSARY;
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
        final Overtime overtime = new Overtime(savedPerson, now, now.plusDays(2), BigDecimal.ONE);
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
        sut.save(new Overtime(savedPerson, now, now.plusDays(2), new BigDecimal("3")));
        sut.save(new Overtime(savedPerson, now.plusDays(5), now.plusDays(10), new BigDecimal("0.5")));
        sut.save(new Overtime(savedPerson, now.minusDays(8), now.minusDays(4), new BigDecimal("-1")));

        // Overtime for other person
        sut.save(new Overtime(savedOtherPerson, now.plusDays(5), now.plusDays(10), new BigDecimal("5")));

        final BigDecimal totalHours = sut.calculateTotalHoursForPerson(person);
        assertThat(totalHours).isNotNull();
        assertThat(totalHours.setScale(1, UNNECESSARY)).isEqualTo(new BigDecimal("2.5").setScale(1, UNNECESSARY));
    }

    @Test
    void ensureReturnsNullAsTotalOvertimeIfPersonHasNoOvertimeRecords() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        personService.save(person);

        final BigDecimal totalHours = sut.calculateTotalHoursForPerson(person);
        assertThat(totalHours).isNull();
    }

    @Test
    void ensureReturnsAllRecordsWithStartOrEndDateInTheGivenYear() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.save(person);

        // records for 2015
        sut.save(new Overtime(savedPerson, of(2014, 12, 30), of(2015, 1, 3), new BigDecimal("1")));
        sut.save(new Overtime(savedPerson, of(2015, 10, 5), of(2015, 10, 20), new BigDecimal("2")));
        sut.save(new Overtime(savedPerson, of(2015, 12, 28), of(2016, 1, 6), new BigDecimal("3")));

        // record for 2014
        sut.save(new Overtime(savedPerson, of(2014, 12, 5), of(2014, 12, 31), new BigDecimal("4")));

        final List<Overtime> overtimes = sut.findByPersonAndPeriod(savedPerson, of(2015, 1, 1), of(2015, 12, 31));
        assertThat(overtimes).hasSize(3);
        assertThat(overtimes.get(0).getStartDate()).isEqualTo(of(2015, 12, 28));
        assertThat(overtimes.get(1).getStartDate()).isEqualTo(of(2015, 10, 5));
        assertThat(overtimes.get(2).getStartDate()).isEqualTo(of(2014, 12, 30));
        assertThat(overtimes.get(0).getDuration()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(overtimes.get(1).getDuration()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(overtimes.get(2).getDuration()).isEqualTo(BigDecimal.valueOf(1));
    }
}
