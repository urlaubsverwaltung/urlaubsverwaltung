package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteCommentDTOTest {

    @Test
    void happyPathDTOToSickNoteCommentEntity() {
        Instant now = Instant.now();
        Person commentator = new Person();

        final SickNoteCommentDTO dto = new SickNoteCommentDTO(now, "Test comment", SickNoteCommentActionDTO.SUBMITTED, "externalId");

        final SickNoteCommentEntity entity = dto.toSickNoteCommentEntity(commentator, 1L);

        assertThat(entity.getSickNoteId()).isEqualTo(1L);
        assertThat(entity.getAction()).isEqualTo(dto.sickNoteCommentAction().toSickNoteCommentAction());
        assertThat(entity.getPerson()).isEqualTo(commentator);
        assertThat(entity.getText()).isEqualTo(dto.text());
        assertThat(entity.getDate()).isEqualTo(dto.date().truncatedTo(ChronoUnit.DAYS));
    }

    @Test
    void handlesNoText() {

        final SickNoteCommentDTO dto = new SickNoteCommentDTO(Instant.now(), null, SickNoteCommentActionDTO.SUBMITTED, "externalId");

        final SickNoteCommentEntity entity = dto.toSickNoteCommentEntity(new Person(), 1L);

        assertThat(entity.getText()).isEmpty();
    }

}
