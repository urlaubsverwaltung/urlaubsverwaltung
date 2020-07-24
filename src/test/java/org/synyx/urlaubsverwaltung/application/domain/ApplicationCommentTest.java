package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;


/**
 * Unit test for {@link ApplicationComment}.
 */
class ApplicationCommentTest {

    @Test
    void ensureDateIsSetOnInitialization() {

        Consumer<ApplicationComment> assertDateIsSetToToday = (comment) -> {
            Assert.assertNotNull("Date should be set", comment.getDate());
            Assert.assertEquals("Date should be set to today", LocalDate.now(UTC), comment.getDate());
        };

        assertDateIsSetToToday.accept(new ApplicationComment());
        assertDateIsSetToToday.accept(new ApplicationComment(DemoDataCreator.createPerson()));
    }


    @Test
    void ensureCanBeInitializedWithPerson() {

        Person commentingPerson = DemoDataCreator.createPerson();

        ApplicationComment comment = new ApplicationComment(commentingPerson);

        Assert.assertNotNull("Commenting person should be set", comment.getPerson());
        Assert.assertEquals("Wrong commenting person", commentingPerson, comment.getPerson());
    }
}
