package org.synyx.urlaubsverwaltung.comment;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;


class AbstractCommentTest {

    @Test
    void ensureHasDateSetAfterInitialization() {

        final TestComment comment = new TestComment();
        assertThat(comment.getDate()).isEqualTo(LocalDate.now(UTC));
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

    private static class TestComment extends AbstractComment {
        private TestComment() {
            super();
        }
    }
}
