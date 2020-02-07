package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Consumer;


/**
 * Unit test for {@link ApplicationComment}.
 */
class ApplicationCommentTest {

    @Test
    void ensureDateIsSetOnInitialization() {

        Consumer<ApplicationComment> assertDateIsSetToToday = (comment) -> {
            Assert.assertNotNull("Date should be set", comment.getDate());
            Assert.assertEquals("Date should be set to today", LocalDate.now(Clock.systemUTC()), comment.getDate());
        };

        assertDateIsSetToToday.accept(new ApplicationComment());
        assertDateIsSetToToday.accept(new ApplicationComment(new Person("muster", "Muster", "Marlene", "muster@example.org")));
    }


    @Test
    void ensureCanBeInitializedWithPerson() {

        Person commentingPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");

        ApplicationComment comment = new ApplicationComment(commentingPerson);

        Assert.assertNotNull("Commenting person should be set", comment.getPerson());
        Assert.assertEquals("Wrong commenting person", commentingPerson, comment.getPerson());
    }
}
