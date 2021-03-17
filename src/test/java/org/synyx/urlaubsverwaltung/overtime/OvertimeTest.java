package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.synyx.urlaubsverwaltung.person.Person;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


class OvertimeTest {

    @Test
    void ensureReturnsCorrectStartDate() {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        Overtime overtime = new Overtime(person, now, now.plusDays(2), Duration.ofHours(1));
        assertThat(overtime.getStartDate()).isEqualTo(now);
    }

    @Test
    void ensureReturnsCorrectEndDate() {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        Overtime overtime = new Overtime(person, now.minusDays(2), now, Duration.ofHours(1));
        assertThat(overtime.getEndDate()).isEqualTo(now);
    }

    @Test
    void ensureSetLastModificationDateOnInitialization() {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        Overtime overtime = new Overtime(person, now.minusDays(2), now.plusDays(4), Duration.ofHours(1));
        assertThat(overtime.getLastModificationDate()).isEqualTo(now);
    }

    @Test
    void ensureThrowsIfGettingStartDateOnACorruptedOvertime() throws IllegalAccessException {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        Overtime overtime = new Overtime(person, now.minusDays(2), now, Duration.ofHours(1));

        Field startDateField = ReflectionUtils.findField(Overtime.class, "startDate");
        startDateField.setAccessible(true);
        startDateField.set(overtime, null);

        assertThatIllegalStateException().isThrownBy(overtime::getStartDate);
    }

    @Test
    void ensureThrowsIfGettingEndDateOnACorruptedOvertime() throws IllegalAccessException {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        Overtime overtime = new Overtime(person, now.minusDays(2), now, Duration.ofHours(1));

        Field endDateField = ReflectionUtils.findField(Overtime.class, "endDate");
        endDateField.setAccessible(true);
        endDateField.set(overtime, null);

        assertThatIllegalStateException().isThrownBy(overtime::getEndDate);
    }

    @Test
    void ensureThrowsIfGettingLastModificationDateOnACorruptedOvertime() throws IllegalAccessException {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        Overtime overtime = new Overtime(person, now.minusDays(2), now, Duration.ofHours(1));

        Field lastModificationDateField = ReflectionUtils.findField(Overtime.class, "lastModificationDate");
        lastModificationDateField.setAccessible(true);
        lastModificationDateField.set(overtime, null);

        assertThatIllegalStateException().isThrownBy(overtime::getLastModificationDate);
    }

    @Test
    void ensureCallingOnUpdateChangesLastModificationDate() throws IllegalAccessException {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        LocalDate now = LocalDate.now(UTC);

        Overtime overtime = new Overtime(person, now.minusDays(2), now, Duration.ofHours(1));

        // Simulate that the overtime record has been created to an earlier time
        Field lastModificationDateField = ReflectionUtils.findField(Overtime.class, "lastModificationDate");
        lastModificationDateField.setAccessible(true);
        lastModificationDateField.set(overtime, now.minusDays(3));

        assertThat(overtime.getLastModificationDate()).isEqualTo(now.minusDays(3));
        overtime.onUpdate();
        assertThat(overtime.getLastModificationDate()).isEqualTo(now);
    }

    @Test
    void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));
        final Overtime overtime = new Overtime(person, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(1);

        final String overtimeToString = overtime.toString();
        assertThat(overtimeToString).isEqualTo("Overtime{id=1, startDate=-999999999-01-01, endDate=+999999999-12-31, duration=PT10H, person=Person{id='10'}}");
    }

    @Test
    void equals() {
        final Overtime overtimeOne = new Overtime();
        overtimeOne.setId(1);

        final Overtime overtimeOneOne = new Overtime();
        overtimeOneOne.setId(1);

        final Overtime overtimeTwo = new Overtime();
        overtimeTwo.setId(2);

        assertThat(overtimeOne)
            .isEqualTo(overtimeOne)
            .isEqualTo(overtimeOneOne)
            .isNotEqualTo(overtimeTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final Overtime overtimeOne = new Overtime();
        overtimeOne.setId(1);

        assertThat(overtimeOne.hashCode()).isEqualTo(32);
    }
}
