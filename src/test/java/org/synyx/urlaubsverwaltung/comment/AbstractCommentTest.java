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

    private class TestComment extends AbstractComment {

        private TestComment(Clock clock) {
            super(clock);
        }
    }
}
