package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeCommentDTOTest {

    @Test
    void happyPathOvertimeCommentToDTO() {
        OvertimeComment overtimeComment = new OvertimeComment(Clock.fixed(Instant.now(), ZoneId.systemDefault()));
        overtimeComment.setText("text");
        overtimeComment.setAction(OvertimeCommentAction.CREATED);
        Person person = new Person();
        person.setUsername("username");
        overtimeComment.setPerson(person);

        OvertimeCommentDTO dto = OvertimeCommentDTO.of(overtimeComment);

        assertThat(dto.date()).isEqualTo(overtimeComment.getDate());
        assertThat(dto.text()).isEqualTo(overtimeComment.getText());
        assertThat(dto.action()).isEqualTo(OvertimeCommentActionDTO.CREATED);
        assertThat(dto.externalIdOfCommentAuthor()).isEqualTo(overtimeComment.getPerson().getUsername());
    }

    @Test
    void happyPathDTOToOvertimeComment() {
        OvertimeCommentDTO dto = new OvertimeCommentDTO(Instant.now(), "text", OvertimeCommentActionDTO.CREATED, "username");
        Person person = new Person();
        person.setUsername("username");
        Overtime overtime = new Overtime(null, null, null, null);

        OvertimeComment overtimeComment = dto.toOvertimeComment(overtime, person);

        assertThat(overtimeComment.getPerson().getUsername()).isEqualTo(dto.externalIdOfCommentAuthor());
        assertThat(overtimeComment.getOvertime()).isEqualTo(overtime);
        assertThat(overtimeComment.getAction()).isEqualTo(OvertimeCommentAction.CREATED);
        assertThat(overtimeComment.getText()).isEqualTo(dto.text());
        assertThat(overtimeComment.getDate()).isEqualTo(dto.date().truncatedTo(ChronoUnit.DAYS));
    }
}
