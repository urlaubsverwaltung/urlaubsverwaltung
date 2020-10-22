package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Consumer;

import static java.time.temporal.ChronoUnit.DAYS;


/**
 * Unit test for {@link ApplicationComment}.
 */
class ApplicationCommentTest {

    private final Clock clock = Clock.systemUTC();

    @Test
    void ensureDateIsSetOnInitialization() {

        Consumer<ApplicationComment> assertDateIsSetToToday = (comment) -> {
            Assert.assertNotNull("Date should be set", comment.getDate());
            Assert.assertEquals("Date should be set to today",
                Instant.now(Clock.systemUTC()).truncatedTo(DAYS), comment.getDate());
        };

        assertDateIsSetToToday.accept(new ApplicationComment(clock));
        assertDateIsSetToToday.accept(new ApplicationComment(new Person("muster", "Muster", "Marlene", "muster@example.org"), clock));
    }


    @Test
    void ensureCanBeInitializedWithPerson() {

        Person commentingPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");

        ApplicationComment comment = new ApplicationComment(commentingPerson, clock);

        Assert.assertNotNull("Commenting person should be set", comment.getPerson());
        Assert.assertEquals("Wrong commenting person", commentingPerson, comment.getPerson());
    }
}
