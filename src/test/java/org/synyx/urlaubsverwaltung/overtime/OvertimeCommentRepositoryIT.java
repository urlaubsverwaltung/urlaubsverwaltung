package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.COMMENTED;

@SpringBootTest
@Transactional
class OvertimeCommentRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private OvertimeCommentRepository sut;

    @Autowired
    private PersonService personService;
    @Autowired
    private OvertimeService overtimeService;

    @Test
    void ensureToGetOvertimeCommentsInCorrectOrder() {

        final LocalDate now = LocalDate.now(UTC);

        final Person person = personService.create("batman", "Bruce", "Wayne", "batman@example.org");

        final OvertimeEntity overtime = new OvertimeEntity(person, now, now, Duration.ofHours(4));
        final OvertimeEntity savedOvertime = overtimeService.save(overtime, Optional.empty(), person);

        final OvertimeComment first = overtimeService.getCommentsForOvertime(savedOvertime).getFirst();
        final OvertimeComment second = overtimeService.saveComment(savedOvertime, COMMENTED, "second", person);
        final OvertimeComment third = overtimeService.saveComment(savedOvertime, COMMENTED, "third", person);
        final OvertimeComment fourth = overtimeService.saveComment(savedOvertime, COMMENTED, "fourth", person);

        final List<OvertimeCommentEntity> overtimeComments = sut.findByOvertimeOrderByIdDesc(savedOvertime);

        assertThat(overtimeComments).satisfiesExactly(
            entity -> assertThat(entity.getId()).isEqualTo(fourth.id().value()),
            entity -> assertThat(entity.getId()).isEqualTo(third.id().value()),
            entity -> assertThat(entity.getId()).isEqualTo(second.id().value()),
            entity -> assertThat(entity.getId()).isEqualTo(first.id().value())
        );
    }
}
