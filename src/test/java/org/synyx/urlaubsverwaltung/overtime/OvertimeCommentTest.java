package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createOvertimeRecord;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;


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

        OvertimeComment comment = new OvertimeComment(author, overtime, OvertimeCommentAction.CREATED, clock);

        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getOvertime()).isEqualTo(overtime);
        assertThat(comment.getAction()).isEqualTo(OvertimeCommentAction.CREATED);
        assertThat(comment.getDate()).isEqualTo(Instant.now(clock).truncatedTo(DAYS));
        assertThat(comment.getText()).isNull();
    }
}
