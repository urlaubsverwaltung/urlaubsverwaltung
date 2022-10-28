package org.synyx.urlaubsverwaltung.comment;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;


class AbstractCommentTest {

    @Test
    void ensureHasDateSetAfterInitialization() {

        final Clock clock = Clock.systemUTC();
        final TestComment comment = new TestComment(clock);
        assertThat(comment.getDate()).isEqualTo(Instant.now(clock).truncatedTo(DAYS));
    }

    @Test
    void ensureThrowsIfGettingDateOnACorruptedComment() throws IllegalAccessException {

        final TestComment comment = new TestComment(Clock.systemUTC());

        final Field dateField = ReflectionUtils.findField(AbstractComment.class, "date");
        dateField.setAccessible(true);
        dateField.set(comment, null);

        assertThatIllegalStateException()
            .isThrownBy(comment::getDate);
    }

    @Test
    void equals() {
        final TestComment commentOne = new TestComment(Clock.systemUTC());
        commentOne.setId(1);

        final TestComment commentOneOne = new TestComment(Clock.systemUTC());
        commentOneOne.setId(1);

        final TestComment commentTwo = new TestComment(Clock.systemUTC());
        commentTwo.setId(2);

        assertThat(commentOne)
            .isEqualTo(commentOne)
            .isEqualTo(commentOneOne)
            .isNotEqualTo(commentTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final TestComment commentOne = new TestComment(Clock.systemUTC());
        commentOne.setId(1);

        assertThat(commentOne.hashCode()).isEqualTo(32);
    }

    private class TestComment extends AbstractComment {

        private TestComment(Clock clock) {
            super(clock);
        }
    }
}
