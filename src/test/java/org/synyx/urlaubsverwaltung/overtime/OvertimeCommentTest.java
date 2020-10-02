package org.synyx.urlaubsverwaltung.overtime;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createOvertimeRecord;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeAction.CREATED;
import static java.time.temporal.ChronoUnit.DAYS;


class OvertimeCommentTest {

    private final Clock clock = Clock.systemUTC();

    @Test
    void ensureThrowsOnNullPerson() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new OvertimeComment(null, createOvertimeRecord(), CREATED, clock));
    }


    @Test
    void ensureThrowsOnNullOvertime() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new OvertimeComment(new Person("muster", "Muster", "Marlene", "muster@example.org"), null, CREATED, clock));
    }


    @Test
    void ensureThrowsOnNullAction() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new OvertimeComment(new Person("muster", "Muster", "Marlene", "muster@example.org"), createOvertimeRecord(), null, clock));
    }


    @Test
    void ensureCorrectPropertiesAfterInitialization() {

        Person author = new Person("muster", "Muster", "Marlene", "muster@example.org");
        Overtime overtime = createOvertimeRecord();

        OvertimeComment comment = new OvertimeComment(author, overtime, OvertimeAction.CREATED, clock);

        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong overtime record", overtime, comment.getOvertime());
        Assert.assertEquals("Wrong action", OvertimeAction.CREATED, comment.getAction());
        Assert.assertEquals("Wrong date", Instant.now(clock).truncatedTo(DAYS), comment.getDate());

        Assert.assertNull("Should not be set", comment.getText());
    }
}
