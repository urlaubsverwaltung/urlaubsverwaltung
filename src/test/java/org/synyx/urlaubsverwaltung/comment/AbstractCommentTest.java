package org.synyx.urlaubsverwaltung.comment;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;


public class AbstractCommentTest {

    @Test
    public void ensureHasDateSetAfterInitialization() {

        TestComment comment = new TestComment();

        Assert.assertNotNull("Should not be null", comment.getDate());
        Assert.assertEquals("Wrong date", DateMidnight.now(), comment.getDate());
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfGettingDateOnACorruptedComment() throws IllegalAccessException {

        TestComment comment = new TestComment();

        Field dateField = ReflectionUtils.findField(AbstractComment.class, "date");
        dateField.setAccessible(true);
        dateField.set(comment, null);

        comment.getDate();
    }

    private class TestComment extends AbstractComment {

        private TestComment() {

            super();
        }
    }
}
