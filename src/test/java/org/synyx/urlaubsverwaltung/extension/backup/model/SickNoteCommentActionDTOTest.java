package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteCommentActionDTOTest {

    @ParameterizedTest
    @EnumSource(SickNoteCommentAction.class)
    void happyPathSickNoteCOmmentActionToDTO(SickNoteCommentAction sickNoteCommentAction) {
        SickNoteCommentActionDTO dto = SickNoteCommentActionDTO.valueOf(sickNoteCommentAction.name());
        assertThat(dto).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(SickNoteCommentActionDTO.class)
    void happyPathDTOToSickNoteCommentAction(SickNoteCommentActionDTO dto) {
        SickNoteCommentAction sickNoteCommentAction = dto.toSickNoteCommentAction();
        assertThat(sickNoteCommentAction).isNotNull();
    }

}
