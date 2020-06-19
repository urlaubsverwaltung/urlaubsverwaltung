package org.synyx.urlaubsverwaltung.overtime;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;

import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;


public class OvertimeCommentTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPerson() {

        new OvertimeComment(null, DemoDataCreator.createOvertimeRecord(), OvertimeAction.CREATED);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullOvertime() {

        new OvertimeComment(DemoDataCreator.createPerson(), null, OvertimeAction.CREATED);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullAction() {

        new OvertimeComment(DemoDataCreator.createPerson(), DemoDataCreator.createOvertimeRecord(), null);
    }


    @Test
    public void ensureCorrectPropertiesAfterInitialization() {

        Person author = DemoDataCreator.createPerson();
        Overtime overtime = DemoDataCreator.createOvertimeRecord();

        OvertimeComment comment = new OvertimeComment(author, overtime, OvertimeAction.CREATED);

        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong overtime record", overtime, comment.getOvertime());
        Assert.assertEquals("Wrong action", OvertimeAction.CREATED, comment.getAction());
        Assert.assertEquals("Wrong date", LocalDate.now(UTC), comment.getDate());

        Assert.assertNull("Should not be set", comment.getText());
    }
}
