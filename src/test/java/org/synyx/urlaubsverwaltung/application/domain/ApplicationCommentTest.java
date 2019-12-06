package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;


/**
 * Unit test for {@link ApplicationComment}.
 */
public class ApplicationCommentTest {

    @Test
    public void ensureDateIsSetOnInitialization() {

        Consumer<ApplicationComment> assertDateIsSetToToday = (comment) -> {
            Assert.assertNotNull("Date should be set", comment.getDate());
            Assert.assertEquals("Date should be set to today", LocalDate.now(UTC), comment.getDate());
        };

        assertDateIsSetToToday.accept(new ApplicationComment());
        assertDateIsSetToToday.accept(new ApplicationComment(TestDataCreator.createPerson()));
    }


    @Test
    public void ensureCanBeInitializedWithPerson() {

        Person commentingPerson = TestDataCreator.createPerson();

        ApplicationComment comment = new ApplicationComment(commentingPerson);

        Assert.assertNotNull("Commenting person should be set", comment.getPerson());
        Assert.assertEquals("Wrong commenting person", commentingPerson, comment.getPerson());
    }
}
