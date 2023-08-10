package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteCommentEntityTest {

    @Test
    void equals() {
        final SickNoteCommentEntity commentOne = new SickNoteCommentEntity(Clock.systemUTC());
        commentOne.setId(1L);

        final SickNoteCommentEntity commentOneOne = new SickNoteCommentEntity(Clock.systemUTC());
        commentOneOne.setId(1L);

        final SickNoteCommentEntity commentTwo = new SickNoteCommentEntity(Clock.systemUTC());
        commentTwo.setId(2L);

        assertThat(commentOne)
            .isEqualTo(commentOne)
            .isEqualTo(commentOneOne)
            .isNotEqualTo(commentTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final SickNoteCommentEntity commentOne = new SickNoteCommentEntity(Clock.systemUTC());
        commentOne.setId(1L);

        assertThat(commentOne.hashCode()).isEqualTo(32);
    }
}
