package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createOvertimeRecord;

class OvertimeCommentEntityTest {

    private final Clock clock = Clock.systemUTC();

    @Test
    void ensureCorrectPropertiesAfterInitialization() {

        Person author = new Person("muster", "Muster", "Marlene", "muster@example.org");
        OvertimeEntity overtime = createOvertimeRecord();

        OvertimeCommentEntity comment = new OvertimeCommentEntity(author, overtime, OvertimeCommentAction.CREATED, clock);

        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getOvertime()).isEqualTo(overtime);
        assertThat(comment.getAction()).isEqualTo(OvertimeCommentAction.CREATED);
        assertThat(comment.getDate()).isEqualTo(Instant.now(clock).truncatedTo(DAYS));
        assertThat(comment.getText()).isNull();
    }

    @Test
    void equals() {
        final OvertimeCommentEntity commentOne = new OvertimeCommentEntity(Clock.systemUTC());
        commentOne.setId(1L);

        final OvertimeCommentEntity commentOneOne = new OvertimeCommentEntity(Clock.systemUTC());
        commentOneOne.setId(1L);

        final OvertimeCommentEntity commentTwo = new OvertimeCommentEntity(Clock.systemUTC());
        commentTwo.setId(2L);

        assertThat(commentOne)
            .isEqualTo(commentOne)
            .isEqualTo(commentOneOne)
            .isNotEqualTo(commentTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final OvertimeCommentEntity commentOne = new OvertimeCommentEntity(Clock.systemUTC());
        commentOne.setId(1L);

        assertThat(commentOne.hashCode()).isEqualTo(32);
    }
}
