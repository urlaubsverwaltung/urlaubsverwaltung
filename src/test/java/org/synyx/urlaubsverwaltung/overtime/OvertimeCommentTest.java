package org.synyx.urlaubsverwaltung.overtime;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createOvertimeRecord;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeAction.CREATED;


class OvertimeCommentTest {

    @Test
    void ensureThrowsOnNullPerson() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new OvertimeComment(null, createOvertimeRecord(), CREATED));
    }


    @Test
    void ensureThrowsOnNullOvertime() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new OvertimeComment(createPerson(), null, CREATED));
    }


    @Test
    void ensureThrowsOnNullAction() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new OvertimeComment(createPerson(), createOvertimeRecord(), null));
    }


    @Test
    void ensureCorrectPropertiesAfterInitialization() {

        Person author = createPerson();
        Overtime overtime = createOvertimeRecord();

        OvertimeComment comment = new OvertimeComment(author, overtime, CREATED);

        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong overtime record", overtime, comment.getOvertime());
        Assert.assertEquals("Wrong action", CREATED, comment.getAction());
        Assert.assertEquals("Wrong date", LocalDate.now(UTC), comment.getDate());

        Assert.assertNull("Should not be set", comment.getText());
    }
}
