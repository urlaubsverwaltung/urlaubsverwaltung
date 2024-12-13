package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationCommentDTOTest {

    @Test
    void happyPath() {
        ApplicationCommentDTO applicationCommentDTO = new ApplicationCommentDTO(ApplicationCommentActionDTO.EDITED, "externalId123", Instant.parse("2023-01-01T10:00:00Z"), "This is a comment");
        Person author = new Person();
        Long applicationId = 1L;

        ApplicationCommentEntity applicationCommentEntity = applicationCommentDTO.toApplicationCommentEntity(author, applicationId);

        assertThat(applicationCommentEntity.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationCommentEntity.getAction()).isEqualTo(ApplicationCommentAction.valueOf(applicationCommentDTO.action().name()));
        assertThat(applicationCommentEntity.getPerson()).isEqualTo(author);
        assertThat(applicationCommentEntity.getText()).isEqualTo(applicationCommentDTO.text());
        assertThat(applicationCommentEntity.getDate()).isEqualTo(Instant.parse("2023-01-01T00:00:00Z"));
    }

    @Test
    void applicationCommentDTOToEntityWithNullText() {
        ApplicationCommentDTO applicationCommentDTO = new ApplicationCommentDTO(ApplicationCommentActionDTO.EDITED, "externalId123", Instant.parse("2023-01-01T10:00:00Z"), null);

        ApplicationCommentEntity applicationCommentEntity = applicationCommentDTO.toApplicationCommentEntity(new Person(), 1L);

        assertThat(applicationCommentEntity.getText()).isEmpty();
    }
}
