package org.synyx.urlaubsverwaltung.application.comment;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Consumer;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link ApplicationComment}.
 */
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
}
