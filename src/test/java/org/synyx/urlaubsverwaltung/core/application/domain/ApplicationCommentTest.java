package org.synyx.urlaubsverwaltung.core.application.domain;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.function.Consumer;


/**
 * Unit test for {@link ApplicationComment}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationCommentTest {

    @Test
    public void ensureDateIsSetOnInitialization() {

        Consumer<ApplicationComment> assertDateIsSetToToday = (comment) -> {
            Assert.assertNotNull("Date should be set", comment.getDate());
            Assert.assertEquals("Date should be set to today", DateMidnight.now(), comment.getDate());
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
