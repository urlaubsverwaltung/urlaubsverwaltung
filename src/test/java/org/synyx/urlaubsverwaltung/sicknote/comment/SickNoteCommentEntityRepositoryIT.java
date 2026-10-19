package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction.COMMENTED;

@SpringBootTest
@Transactional
class SickNoteCommentEntityRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private SickNoteCommentEntityRepository sut;

    @Autowired
    private PersonService personService;

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private SickNoteCommentService sickNoteCommentService;

    @Test
    void ensureToGetSickNoteCommentsInCorrectOrder() {

        final LocalDate now = LocalDate.now(UTC);

        final Person person = personService.create("batman", "Bruce", "Wayne", "batman@example.org");
        final SickNote sickNote = sickNoteService.save(SickNote.builder().person(person).startDate(now).endDate(now).build());

        final SickNoteCommentEntity first = sickNoteCommentService.create(sickNote, COMMENTED, person, "first");
        final SickNoteCommentEntity second = sickNoteCommentService.create(sickNote, COMMENTED, person, "second");
        final SickNoteCommentEntity third = sickNoteCommentService.create(sickNote, COMMENTED, person, "third");
        final SickNoteCommentEntity fourth = sickNoteCommentService.create(sickNote, COMMENTED, person, "fourth");

        final List<SickNoteCommentEntity> sickNoteComments = sut.findBySickNoteIdOrderByIdDesc(sickNote.getId());
        assertThat(sickNoteComments)
            .containsExactly(fourth, third, second, first);
    }

    private List<SickNoteCommentEntity> getAllComments() {
        return StreamSupport.stream(sut.findAll().spliterator(), false).toList();
    }
}
