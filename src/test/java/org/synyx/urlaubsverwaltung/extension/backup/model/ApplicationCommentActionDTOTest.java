package org.synyx.urlaubsverwaltung.extension.backup.model;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationCommentActionDTOTest {

    @ParameterizedTest
    @EnumSource(ApplicationCommentAction.class)
    void happyPathToDTO(ApplicationCommentAction action) {
        ApplicationCommentActionDTO dto = ApplicationCommentActionDTO.valueOf(action.name());
        assertThat(dto).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(ApplicationCommentActionDTO.class)
    void happyPathFromDTO(ApplicationCommentActionDTO dto) {
        ApplicationCommentAction lala = dto.toApplicationCommentAction();
        assertThat(lala).isNotNull();
    }
}
