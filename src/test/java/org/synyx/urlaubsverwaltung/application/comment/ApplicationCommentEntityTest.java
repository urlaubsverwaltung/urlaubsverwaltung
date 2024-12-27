package org.synyx.urlaubsverwaltung.application.comment;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Consumer;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;


class ApplicationCommentEntityTest {

    private final Clock clock = Clock.systemUTC();

    @Test
    void ensureDateIsSetOnInitialization() {

        Consumer<ApplicationCommentEntity> assertDateIsSetToToday = (comment) -> {
            assertThat(comment.getDate()).isEqualTo(Instant.now(Clock.systemUTC()).truncatedTo(DAYS));
        };

        assertDateIsSetToToday.accept(new ApplicationCommentEntity(clock));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        assertDateIsSetToToday.accept(new ApplicationCommentEntity(person, clock));
    }

    @Test
    void ensureCanBeInitializedWithPerson() {

        Person commentingPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        ApplicationCommentEntity comment = new ApplicationCommentEntity(commentingPerson, clock);

        assertThat(comment.getPerson()).isEqualTo(commentingPerson);
    }

    @Test
    void equals() {
        final ApplicationCommentEntity commentOne = new ApplicationCommentEntity(Clock.systemUTC());
        commentOne.setId(1L);

        final ApplicationCommentEntity commentOneOne = new ApplicationCommentEntity(Clock.systemUTC());
        commentOneOne.setId(1L);

        final ApplicationCommentEntity commentTwo = new ApplicationCommentEntity(Clock.systemUTC());
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
        final ApplicationCommentEntity commentOne = new ApplicationCommentEntity(Clock.systemUTC());
        commentOne.setId(1L);

        assertThat(commentOne.hashCode()).isEqualTo(32);
    }
}
