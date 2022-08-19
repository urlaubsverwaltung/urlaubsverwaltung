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
import java.util.Optional;

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
        final Person savedPerson = personService.create(person);

        final LocalDate now = LocalDate.now(UTC);
        final Overtime overtime = new Overtime(savedPerson, now, now.plusDays(2), Duration.ofHours(1));
        assertThat(overtime.getId()).isNull();

        sut.save(overtime);
        assertThat(overtime.getId()).isNotNull();
    }

    @Test
    void ensureCountsTotalHoursCorrectly() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.create(person);

        final Person otherPerson = new Person("freddy", "Gwin", "freddy", "gwin@example.org");
        final Person savedOtherPerson = personService.create(otherPerson);

        final LocalDate now = LocalDate.now(UTC);

        // Overtime for person
        sut.save(new Overtime(savedPerson, now, now.plusDays(2), Duration.ofHours(3)));
        sut.save(new Overtime(savedPerson, now.plusDays(5), now.plusDays(10), Duration.ofMinutes(30)));
        sut.save(new Overtime(savedPerson, now.minusDays(8), now.minusDays(4), Duration.ofHours(-1)));

        // Overtime for other person
        sut.save(new Overtime(savedOtherPerson, now.plusDays(5), now.plusDays(10), Duration.ofHours(5)));

        final Optional<Double> totalHours = sut.calculateTotalHoursForPerson(person);
        assertThat(totalHours).hasValue(2.5);
    }

    @Test
    void ensureCalculateTotalHoursForPersons() {

        final Person person = new Person("batman", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.create(person);
        sut.save(new Overtime(savedPerson, of(2015, 10, 5), of(2015, 10, 20), Duration.ofHours(2)));
        sut.save(new Overtime(savedPerson, of(2015, 12, 28), of(2016, 1, 6), Duration.ofHours(3)));
        sut.save(new Overtime(savedPerson, of(2014, 12, 30), of(2015, 1, 3), Duration.ofHours(1)));

        final Person person2 = new Person("joker", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson2 = personService.create(person2);
        sut.save(new Overtime(savedPerson2, of(2015, 10, 5), of(2015, 10, 20), Duration.ofHours(1)));
        sut.save(new Overtime(savedPerson2, of(2015, 12, 28), of(2016, 1, 6), Duration.ofHours(1)));

        // should not be in result set
        final Person person3 = new Person("robin", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson3 = personService.create(person3);
        sut.save(new Overtime(savedPerson3, of(2015, 12, 28), of(2016, 1, 6), Duration.ofHours(42)));

        final List<OvertimeDurationSum> actual = sut.calculateTotalHoursForPersons(List.of(savedPerson, savedPerson2));
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getPerson()).isEqualTo(savedPerson);
        assertThat(actual.get(0).getDurationDouble()).isEqualTo(6);
        assertThat(actual.get(1).getPerson()).isEqualTo(savedPerson2);
        assertThat(actual.get(1).getDurationDouble()).isEqualTo(2);
    }

    @Test
    void ensureCalculateTotalHoursForPersonsDoesNotIncludePersonsWithoutOvertimeReduction() {

        final Person person = new Person("joker", "Muster", "Marlene", "muster@example.org");
        personService.create(person);

        final List<OvertimeDurationSum> actual = sut.calculateTotalHoursForPersons(List.of(person));
        assertThat(actual).isEmpty();
    }

    @Test
    void ensureReturnsNullAsTotalOvertimeIfPersonHasNoOvertimeRecords() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        personService.create(person);

        final Optional<Double> totalHours = sut.calculateTotalHoursForPerson(person);
        assertThat(totalHours).isEmpty();
    }

    @Test
    void ensureReturnsAllRecordsWithStartOrEndDateInTheGivenYear() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.create(person);

        // records to find
        sut.save(new Overtime(savedPerson, of(2015, 10, 5), of(2015, 10, 20), Duration.ofHours(2)));
        sut.save(new Overtime(savedPerson, of(2015, 12, 28), of(2016, 1, 6), Duration.ofHours(3)));

        // record not to find
        sut.save(new Overtime(savedPerson, of(2014, 12, 30), of(2015, 1, 3), Duration.ofHours(1)));
        sut.save(new Overtime(savedPerson, of(2014, 12, 5), of(2014, 12, 31), Duration.ofHours(4)));
        sut.save(new Overtime(savedPerson, of(2014, 12, 5), of(2016, 12, 31), Duration.ofHours(4)));

        final List<Overtime> overtimes = sut.findByPersonAndStartDateBetweenOrderByStartDateDesc(savedPerson, of(2015, 1, 1), of(2015, 12, 31));
        assertThat(overtimes).hasSize(2);
        assertThat(overtimes.get(0).getStartDate()).isEqualTo(of(2015, 12, 28));
        assertThat(overtimes.get(1).getStartDate()).isEqualTo(of(2015, 10, 5));
        assertThat(overtimes.get(0).getDuration()).isEqualTo(Duration.ofHours(3));
        assertThat(overtimes.get(1).getDuration()).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void ensureFindByPersonAndStartDateIsBefore() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.create(person);

        // records starting before 2016
        sut.save(new Overtime(savedPerson, of(2012, 1, 1), of(2012, 1, 3), Duration.ofHours(1)));
        sut.save(new Overtime(savedPerson, of(2014, 12, 30), of(2015, 1, 3), Duration.ofHours(2)));
        sut.save(new Overtime(savedPerson, of(2015, 10, 5), of(2015, 10, 20), Duration.ofHours(3)));
        sut.save(new Overtime(savedPerson, of(2015, 12, 28), of(2016, 1, 6), Duration.ofHours(4)));

        // record after or in 2016
        sut.save(new Overtime(savedPerson, of(2016, 12, 5), of(2016, 12, 31), Duration.ofHours(99)));
        sut.save(new Overtime(savedPerson, of(2016, 1, 1), of(2016, 1, 1), Duration.ofHours(99)));

        final List<Overtime> overtimes = sut.findByPersonAndStartDateIsBefore(savedPerson, of(2016, 1, 1));
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
    void ensureCalculateTotalHoursForPersonsAndStartDateIsBefore() {

        final Person person = personService.create(new Person("muster", "Muster", "Marlene", "muster@example.org"));
        final Person person2 = personService.create(new Person("retsum", "Retsum", "Enelram", "retsum@example.org"));
        final Person person3 = personService.create(new Person("john", "doe", "john", "john@example.org"));

        final List<Person> persons = List.of(person, person2);
        final LocalDate date = LocalDate.of(2022, 2, 1);

        // should be found
        sut.save(new Overtime(person, LocalDate.of(2022, 1, 5), LocalDate.of(2022, 1, 5), Duration.ofHours(2)));
        sut.save(new Overtime(person, LocalDate.of(2022, 1, 31), LocalDate.of(2022, 2, 2), Duration.ofMinutes(90)));
        sut.save(new Overtime(person, LocalDate.of(2021, 12, 28), LocalDate.of(2021, 12, 28), Duration.ofHours(4)));
        sut.save(new Overtime(person2, LocalDate.of(2022, 1, 5), LocalDate.of(2022, 1, 5), Duration.ofMinutes(15)));
        sut.save(new Overtime(person2, LocalDate.of(2022, 1, 31), LocalDate.of(2022, 2, 2), Duration.ofMinutes(30)));

        // should not be found
        sut.save(new Overtime(person, LocalDate.of(2022, 2, 1), LocalDate.of(2022, 2, 1), Duration.ofHours(10)));
        sut.save(new Overtime(person3, LocalDate.of(2022, 1, 5), LocalDate.of(2022, 1, 5), Duration.ofHours(2)));
        sut.save(new Overtime(person3, LocalDate.of(2022, 1, 31), LocalDate.of(2022, 2, 2), Duration.ofMinutes(90)));

        final List<OvertimeDurationSum> actual = sut.calculateTotalHoursForPersonsAndStartDateIsBefore(persons, date);
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getPerson()).isEqualTo(person);
        assertThat(actual.get(0).getDurationDouble()).isEqualTo(7.5);
        assertThat(actual.get(1).getPerson()).isEqualTo(person2);
        assertThat(actual.get(1).getDurationDouble()).isEqualTo(0.75);
    }
}
