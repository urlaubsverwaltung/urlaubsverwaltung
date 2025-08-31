package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.synyx.urlaubsverwaltung.person.Person;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


class OvertimeTest {

    @Test
    void ensureReturnsCorrectStartDate() {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        OvertimeEntity overtime = new OvertimeEntity(person, now, now.plusDays(2), Duration.ofHours(1));
        assertThat(overtime.getStartDate()).isEqualTo(now);
    }

    @Test
    void ensureReturnsCorrectEndDate() {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        OvertimeEntity overtime = new OvertimeEntity(person, now.minusDays(2), now, Duration.ofHours(1));
        assertThat(overtime.getEndDate()).isEqualTo(now);
    }

    @Test
    void ensureSetLastModificationDateOnInitialization() {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        OvertimeEntity overtime = new OvertimeEntity(person, now.minusDays(2), now.plusDays(4), Duration.ofHours(1));
        assertThat(overtime.getLastModificationDate()).isEqualTo(now);
    }

    @Test
    void ensureThrowsIfGettingStartDateOnACorruptedOvertime() throws IllegalAccessException {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        OvertimeEntity overtime = new OvertimeEntity(person, now.minusDays(2), now, Duration.ofHours(1));

        Field startDateField = ReflectionUtils.findField(OvertimeEntity.class, "startDate");
        startDateField.setAccessible(true);
        startDateField.set(overtime, null);

        assertThatIllegalStateException().isThrownBy(overtime::getStartDate);
    }

    @Test
    void ensureThrowsIfGettingEndDateOnACorruptedOvertime() throws IllegalAccessException {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        OvertimeEntity overtime = new OvertimeEntity(person, now.minusDays(2), now, Duration.ofHours(1));

        Field endDateField = ReflectionUtils.findField(OvertimeEntity.class, "endDate");
        endDateField.setAccessible(true);
        endDateField.set(overtime, null);

        assertThatIllegalStateException().isThrownBy(overtime::getEndDate);
    }

    @Test
    void ensureThrowsIfGettingLastModificationDateOnACorruptedOvertime() throws IllegalAccessException {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        OvertimeEntity overtime = new OvertimeEntity(person, now.minusDays(2), now, Duration.ofHours(1));

        Field lastModificationDateField = ReflectionUtils.findField(OvertimeEntity.class, "lastModificationDate");
        lastModificationDateField.setAccessible(true);
        lastModificationDateField.set(overtime, null);

        assertThatIllegalStateException().isThrownBy(overtime::getLastModificationDate);
    }

    @Test
    void ensureCallingOnUpdateChangesLastModificationDate() throws IllegalAccessException {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        OvertimeEntity overtime = new OvertimeEntity(person, now.minusDays(2), now, Duration.ofHours(1));

        // Simulate that the overtime record has been created to an earlier time
        Field lastModificationDateField = ReflectionUtils.findField(OvertimeEntity.class, "lastModificationDate");
        lastModificationDateField.setAccessible(true);
        lastModificationDateField.set(overtime, now.minusDays(3));

        assertThat(overtime.getLastModificationDate()).isEqualTo(now.minusDays(3));
        overtime.onUpdate();
        assertThat(overtime.getLastModificationDate()).isEqualTo(now);
    }

    @Test
    void getDurationByYear() {

        final OvertimeEntity overtime = new OvertimeEntity();
        overtime.setStartDate(LocalDate.of(2022, 12, 30));
        overtime.setEndDate(LocalDate.of(2023, 1, 2));
        overtime.setDuration(Duration.ofHours(20));

        final Map<Integer, Duration> durationByYear = overtime.getDurationByYear();

        assertThat(durationByYear)
            .containsEntry(2022, Duration.ofHours(10))
            .containsEntry(2023, Duration.ofHours(10));
    }

    @Test
    void getTotalDurationBefore() {
        final OvertimeEntity overtime = new OvertimeEntity();
        overtime.setStartDate(LocalDate.of(2022, 12, 30));
        overtime.setEndDate(LocalDate.of(2023, 1, 2));
        overtime.setDuration(Duration.ofHours(20));

        assertThat(overtime.getTotalDurationBefore(2023)).isEqualTo(Duration.ofHours(10));
    }

    @Test
    void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10L);
        person.setPermissions(List.of(USER));
        final OvertimeEntity overtime = new OvertimeEntity(person, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(1L);

        final String overtimeToString = overtime.toString();
        assertThat(overtimeToString).isEqualTo("OvertimeEntity{id=1, startDate=-999999999-01-01, endDate=+999999999-12-31, duration=PT10H, external=false, person=Person{id='10'}}");
    }

    @Test
    void equals() {
        final OvertimeEntity overtimeOne = new OvertimeEntity();
        overtimeOne.setId(1L);

        final OvertimeEntity overtimeOneOne = new OvertimeEntity();
        overtimeOneOne.setId(1L);

        final OvertimeEntity overtimeTwo = new OvertimeEntity();
        overtimeTwo.setId(2L);

        assertThat(overtimeOne)
            .isEqualTo(overtimeOne)
            .isEqualTo(overtimeOneOne)
            .isNotEqualTo(overtimeTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final OvertimeEntity overtimeOne = new OvertimeEntity();
        overtimeOne.setId(1L);

        assertThat(overtimeOne.hashCode()).isEqualTo(32);
    }
}
