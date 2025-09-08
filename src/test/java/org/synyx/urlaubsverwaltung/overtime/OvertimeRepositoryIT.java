package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.of;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OvertimeRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private PersonService personService;

    @Autowired
    private OvertimeRepository sut;

    @Test
    void ensureCanPersistOvertime() {

        final Person person = personService.create("muster", "Marlene", "Muster", "muster@example.org");

        final LocalDate now = LocalDate.now(UTC);
        final OvertimeEntity overtime = new OvertimeEntity(person, now, now.plusDays(2), Duration.ofHours(1));
        assertThat(overtime.getId()).isNull();

        sut.save(overtime);
        assertThat(overtime.getId()).isNotNull();
    }

    @Test
    void ensureCountsTotalHoursCorrectly() {

        final Person person = personService.create("sam", "sam", "smith", "smith@example.org");
        final Person otherPerson = personService.create("freddy", "freddy", "Gwin", "gwin@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Overtime for person
        sut.save(new OvertimeEntity(person, now, now.plusDays(2), Duration.ofHours(3)));
        sut.save(new OvertimeEntity(person, now.plusDays(5), now.plusDays(10), Duration.ofMinutes(30)));
        sut.save(new OvertimeEntity(person, now.minusDays(8), now.minusDays(4), Duration.ofHours(-1)));

        // Overtime for other person
        sut.save(new OvertimeEntity(otherPerson, now.plusDays(5), now.plusDays(10), Duration.ofHours(5)));

        final Optional<Double> totalHours = sut.calculateTotalHoursForPerson(person);
        assertThat(totalHours).hasValue(2.5);
    }

    @Test
    void ensureReturnsNullAsTotalOvertimeIfPersonHasNoOvertimeRecords() {

        final Person person = personService.create("muster", "Marlene", "Muster", "muster@example.org");

        final Optional<Double> totalHours = sut.calculateTotalHoursForPerson(person);
        assertThat(totalHours).isEmpty();
    }

    @Test
    void ensureFindByPersonAndStartDateIsBefore() {

        final Person person = personService.create("muster", "Marlene", "Muster", "muster@example.org");

        // records starting before 2016
        sut.save(new OvertimeEntity(person, of(2012, 1, 1), of(2012, 1, 3), Duration.ofHours(1)));
        sut.save(new OvertimeEntity(person, of(2014, 12, 30), of(2015, 1, 3), Duration.ofHours(2)));
        sut.save(new OvertimeEntity(person, of(2015, 10, 5), of(2015, 10, 20), Duration.ofHours(3)));
        sut.save(new OvertimeEntity(person, of(2015, 12, 28), of(2016, 1, 6), Duration.ofHours(4)));

        // record after or in 2016
        sut.save(new OvertimeEntity(person, of(2016, 12, 5), of(2016, 12, 31), Duration.ofHours(99)));
        sut.save(new OvertimeEntity(person, of(2016, 1, 1), of(2016, 1, 1), Duration.ofHours(99)));

        final List<OvertimeEntity> overtimes = sut.findByPersonAndStartDateIsBefore(person, of(2016, 1, 1));
        assertThat(overtimes).hasSize(4);
        assertThat(overtimes.get(0).getStartDate()).isEqualTo(of(2012, 1, 1));
        assertThat(overtimes.get(0).getDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(overtimes.get(1).getStartDate()).isEqualTo(of(2014, 12, 30));
        assertThat(overtimes.get(1).getDuration()).isEqualTo(Duration.ofHours(2));
        assertThat(overtimes.get(2).getStartDate()).isEqualTo(of(2015, 10, 5));
        assertThat(overtimes.get(2).getDuration()).isEqualTo(Duration.ofHours(3));
        assertThat(overtimes.get(3).getStartDate()).isEqualTo(of(2015, 12, 28));
        assertThat(overtimes.get(3).getDuration()).isEqualTo(Duration.ofHours(4));
    }

    @Test
    void ensureFindByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual() {

        final Person person = personService.create("muster", "Marlene", "Muster", "muster@example.org");
        final Person person2 = personService.create("retsum", "Enelram", "Retsum", "retsum@example.org");
        final Person person3 = personService.create("john", "john", "doe", "john@example.org");

        final List<Person> persons = List.of(person, person2);
        final LocalDate start = LocalDate.of(2022, 2, 1);
        final LocalDate end = LocalDate.of(2022, 3, 1);

        // should be found
        sut.save(new OvertimeEntity(person, start.minusDays(1), start.plusDays(1), Duration.ofHours(1)));
        sut.save(new OvertimeEntity(person, end, end, Duration.ofHours(2)));
        sut.save(new OvertimeEntity(person2, start, start, Duration.ofHours(4)));
        sut.save(new OvertimeEntity(person2, end.minusDays(1), end.plusDays(1), Duration.ofHours(3)));

        // should not be found
        sut.save(new OvertimeEntity(person, start.minusDays(5), start.minusDays(4), Duration.ofHours(10)));
        sut.save(new OvertimeEntity(person3, start, start, Duration.ofHours(10)));

        final List<OvertimeEntity> actual = sut.findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, start, end);

        assertThat(actual).hasSize(4);
        assertThat(actual.get(0).getPerson()).isEqualTo(person);
        assertThat(actual.get(0).getDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(actual.get(1).getPerson()).isEqualTo(person);
        assertThat(actual.get(1).getDuration()).isEqualTo(Duration.ofHours(2));
        assertThat(actual.get(2).getPerson()).isEqualTo(person2);
        assertThat(actual.get(2).getDuration()).isEqualTo(Duration.ofHours(4));
        assertThat(actual.get(3).getPerson()).isEqualTo(person2);
        assertThat(actual.get(3).getDuration()).isEqualTo(Duration.ofHours(3));
    }

    @Test
    void ensureToRetrieveOvertimesByPersonIdAndStartDateAndEndDateAndExternalIsTrue() {

        final Person person = personService.create("muster", "Marlene", "Muster", "muster@example.org");
        final Person person2 = personService.create("retsum", "Enelram", "Retsum", "retsum@example.org");

        final LocalDate date = LocalDate.of(2022, 2, 1);

        // should be found
        sut.save(new OvertimeEntity(person, date, date, Duration.ofHours(1), true));

        // should not be found
        sut.save(new OvertimeEntity(person, date.plusDays(1), date.plusDays(1), Duration.ofHours(12), true));
        sut.save(new OvertimeEntity(person2, date, date, Duration.ofHours(12), true));
        sut.save(new OvertimeEntity(person, date, date, Duration.ofHours(12)));

        final Optional<OvertimeEntity> actual = sut.findByPersonIdAndStartDateAndEndDateAndExternalIsTrue(person.getId(), date, date);

        assertThat(actual).hasValueSatisfying(overtime -> {
            assertThat(overtime.getPerson()).isEqualTo(person);
            assertThat(overtime.getDuration()).isEqualTo(Duration.ofHours(1));
        });
    }
}
