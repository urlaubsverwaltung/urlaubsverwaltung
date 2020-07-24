package org.synyx.urlaubsverwaltung.comment;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;


class AbstractCommentTest {

    @Test
    void ensureHasDateSetAfterInitialization() {

        TestComment comment = new TestComment();

        Assert.assertNotNull("Should not be null", comment.getDate());
        Assert.assertEquals("Wrong date", LocalDate.now(UTC), comment.getDate());
    }

    @Test
    void ensureThrowsIfGettingDateOnACorruptedComment() throws IllegalAccessException {

        TestComment comment = new TestComment();

        Field dateField = ReflectionUtils.findField(AbstractComment.class, "date");
        dateField.setAccessible(true);
        dateField.set(comment, null);

        assertThatIllegalStateException()
            .isThrownBy(comment::getDate);
    }

    private class TestComment extends AbstractComment {

        private TestComment() {

            super();
        }
    }
}
