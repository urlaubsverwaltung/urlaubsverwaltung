package org.synyx.urlaubsverwaltung.core.overtime;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeCommentTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPerson() {

        new OvertimeComment(null, TestDataCreator.createOvertimeRecord(), OvertimeAction.CREATED);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullOvertime() {

        new OvertimeComment(TestDataCreator.createPerson(), null, OvertimeAction.CREATED);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullAction() {

        new OvertimeComment(TestDataCreator.createPerson(), TestDataCreator.createOvertimeRecord(), null);
    }


    @Test
    public void ensureCorrectPropertiesAfterInitialization() {

        Person author = TestDataCreator.createPerson();
        Overtime overtime = TestDataCreator.createOvertimeRecord();

        OvertimeComment comment = new OvertimeComment(author, overtime, OvertimeAction.CREATED);

        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong overtime record", overtime, comment.getOvertime());
        Assert.assertEquals("Wrong action", OvertimeAction.CREATED, comment.getAction());
        Assert.assertEquals("Wrong date", DateMidnight.now(), comment.getDate());

        Assert.assertNull("Should not be set", comment.getText());
    }
}
