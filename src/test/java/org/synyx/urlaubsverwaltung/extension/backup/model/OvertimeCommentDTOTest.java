package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCommentEntity;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCommentId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeEntity;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeCommentDTOTest {

    @Test
    void happyPathOvertimeCommentToDTO() {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("username");

        final Instant timestamp = Instant.now();

        final OvertimeComment overtimeComment = new OvertimeComment(
            new OvertimeCommentId(1L),
            1L,
            OvertimeCommentAction.CREATED,
            Optional.of(person.getIdAsPersonId()),
            timestamp,
            "text"
        );

        final Map<PersonId, Person> personByPersonId = Map.of(person.getIdAsPersonId(), person);

        final OvertimeCommentDTO dto = OvertimeCommentDTO.of(overtimeComment, personByPersonId::get);

        assertThat(dto.date()).isEqualTo(timestamp);
        assertThat(dto.text()).isEqualTo("text");
        assertThat(dto.action()).isEqualTo(OvertimeCommentActionDTO.CREATED);
        assertThat(dto.externalIdOfCommentAuthor()).isEqualTo("username");
    }

    @Test
    void happyPathDTOToOvertimeComment() {
        OvertimeCommentDTO dto = new OvertimeCommentDTO(Instant.now(), "text", OvertimeCommentActionDTO.CREATED, "username");
        Person person = new Person();
        person.setUsername("username");
        OvertimeEntity overtime = new OvertimeEntity(null, null, null, null);

        OvertimeCommentEntity overtimeComment = dto.toOvertimeComment(overtime, person);

        assertThat(overtimeComment.getPerson().getUsername()).isEqualTo(dto.externalIdOfCommentAuthor());
        assertThat(overtimeComment.getOvertime()).isEqualTo(overtime);
        assertThat(overtimeComment.getAction()).isEqualTo(OvertimeCommentAction.CREATED);
        assertThat(overtimeComment.getText()).isEqualTo(dto.text());
        assertThat(overtimeComment.getDate()).isEqualTo(dto.date().truncatedTo(ChronoUnit.DAYS));
    }
}
