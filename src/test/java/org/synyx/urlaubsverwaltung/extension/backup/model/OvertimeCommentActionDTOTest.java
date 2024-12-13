package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeCommentActionDTOTest {

    @ParameterizedTest
    @EnumSource(OvertimeCommentAction.class)
    void overtimeCommentActionDTOEnumMatchesOvertimeCommentAction(OvertimeCommentAction action) {
        OvertimeCommentActionDTO actionDTO = OvertimeCommentActionDTO.valueOf(action.name());
        assertThat(actionDTO).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(OvertimeCommentActionDTO.class)
    void overtimeCommentActionEnumMatchesOvertimeCommentActionDTO(OvertimeCommentActionDTO actionDTO) {
        OvertimeCommentAction action = actionDTO.toOvertimeCommentAction();
        assertThat(action).isNotNull();
    }
}
