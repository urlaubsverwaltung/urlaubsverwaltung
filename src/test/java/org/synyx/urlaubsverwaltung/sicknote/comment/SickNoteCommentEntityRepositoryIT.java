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
    void ensureDeletionBySickNotePerson() {

        final LocalDate now = LocalDate.now(UTC);

        final Person batman = personService.create("batman", "Bruce", "Wayne", "batman@example.org");
        final Person robin = personService.create("robin", "Dick", "Grayson", "robin@example.org");
        final Person alfred = personService.create("alfred", "Alfred", "Pennyworth", "alfred@example.org");

        final SickNote sickNoteBatmanOne = sickNoteService.save(SickNote.builder().person(batman).startDate(now.minusDays(10)).endDate(now.minusDays(10)).build());
        final SickNote sickNoteBatmanTwo = sickNoteService.save(SickNote.builder().person(batman).startDate(now.minusDays(5)).endDate(now.minusDays(5)).build());
        final SickNote sickNoteRobinOne = sickNoteService.save(SickNote.builder().person(robin).startDate(now.minusDays(3)).endDate(now.minusDays(3)).build());

        sickNoteCommentService.create(sickNoteBatmanOne, COMMENTED, robin, "miss you, not");
        sickNoteCommentService.create(sickNoteBatmanOne, COMMENTED, robin, "well... actually...");
        sickNoteCommentService.create(sickNoteBatmanTwo, COMMENTED, robin, "aaaand again");
        sickNoteCommentService.create(sickNoteRobinOne, COMMENTED, alfred, "Get well soon!");

        final List<SickNoteCommentEntity> allCommentsBeforeDelete = getAllComments();
        assertThat(allCommentsBeforeDelete).hasSize(4);

        sut.deleteBySickNotePerson(batman);

        final List<SickNoteCommentEntity> allComments = getAllComments();
        assertThat(allComments).hasSize(1);
        assertThat(allComments.getFirst().getSickNoteId()).isEqualTo(sickNoteRobinOne.getId());
        assertThat(allComments.getFirst().getText()).isEqualTo("Get well soon!");
    }

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
