package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

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

        final PersonId personId = person.getIdAsPersonId();
        final DateRange dateRange = new DateRange(now, now);
        final Overtime savedOvertime = overtimeService.createOvertime(personId, dateRange, Duration.ofHours(4), personId, "");

        final OvertimeComment first = overtimeService.getCommentsForOvertime(savedOvertime.id()).getFirst();
        final OvertimeComment second = overtimeService.saveComment(savedOvertime.id(), COMMENTED, "second", person);
        final OvertimeComment third = overtimeService.saveComment(savedOvertime.id(), COMMENTED, "third", person);
        final OvertimeComment fourth = overtimeService.saveComment(savedOvertime.id(), COMMENTED, "fourth", person);

        final List<OvertimeCommentEntity> overtimeComments = sut.findByOvertimeIdOrderByIdDesc(savedOvertime.id().value());

        assertThat(overtimeComments).satisfiesExactly(
            entity -> assertThat(entity.getId()).isEqualTo(fourth.id().value()),
            entity -> assertThat(entity.getId()).isEqualTo(third.id().value()),
            entity -> assertThat(entity.getId()).isEqualTo(second.id().value()),
            entity -> assertThat(entity.getId()).isEqualTo(first.id().value())
        );
    }
}
