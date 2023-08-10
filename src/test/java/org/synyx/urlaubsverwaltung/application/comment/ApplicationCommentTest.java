package org.synyx.urlaubsverwaltung.application.comment;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Consumer;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;


class ApplicationCommentTest {

    private final Clock clock = Clock.systemUTC();

    @Test
    void ensureDateIsSetOnInitialization() {

        Consumer<ApplicationComment> assertDateIsSetToToday = (comment) -> {
            assertThat(comment.getDate()).isEqualTo(Instant.now(Clock.systemUTC()).truncatedTo(DAYS));
        };

        assertDateIsSetToToday.accept(new ApplicationComment(clock));
        assertDateIsSetToToday.accept(new ApplicationComment(new Person("muster", "Muster", "Marlene", "muster@example.org"), clock));
    }

    @Test
    void ensureCanBeInitializedWithPerson() {

        Person commentingPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        ApplicationComment comment = new ApplicationComment(commentingPerson, clock);

        assertThat(comment.getPerson()).isEqualTo(commentingPerson);
    }

    @Test
    void equals() {
        final ApplicationComment commentOne = new ApplicationComment(Clock.systemUTC());
        commentOne.setId(1L);

        final ApplicationComment commentOneOne = new ApplicationComment(Clock.systemUTC());
        commentOneOne.setId(1L);

        final ApplicationComment commentTwo = new ApplicationComment(Clock.systemUTC());
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
        final ApplicationComment commentOne = new ApplicationComment(Clock.systemUTC());
        commentOne.setId(1L);

        assertThat(commentOne.hashCode()).isEqualTo(32);
    }
}
