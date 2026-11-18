package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.PersonUpdate;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;

import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction.EXTENSION_ACCEPTED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Deliberately not {@code @Transactional}: the service itself must keep its writes together.
 */
@SpringBootTest
class SickNoteExtensionInteractionServiceIT extends SingleTenantTestContainersBase {

    @Autowired
    private SickNoteExtensionInteractionService sut;

    @Autowired
    private SickNoteExtensionService sickNoteExtensionService;
    @Autowired
    private SickNoteService sickNoteService;
    @Autowired
    private PersonService personService;
    @Autowired
    private SickNoteTypeService sickNoteTypeService;

    @MockitoBean
    private SickNoteCommentService commentService;

    private PersonId sickPersonId;
    private PersonId officeId;

    // the test runs without a transaction, so its rows would stay in the database shared by all integration tests
    @AfterEach
    void deleteCreatedPersons() {
        if (sickPersonId != null) {
            personService.delete(sickPersonId, sickPersonId);
        }
        if (officeId != null) {
            personService.delete(officeId, officeId);
        }
    }

    @Test
    void ensureAcceptingAnExtensionWritesNothingWhenOneOfItsWritesFails() {

        final LocalDate today = LocalDate.now(UTC);

        final Person person = personService.create("extension-sick", "Bruce", "Wayne", "extension-sick@example.org");
        sickPersonId = person.getIdAsPersonId();
        final Person office = personService.create("extension-office", "Alfred", "Pennyworth", "extension-office@example.org");
        officeId = office.getIdAsPersonId();
        final Person officeWithRoles = personService.update(office.getIdAsPersonId(), PersonUpdate.ofPermissions(List.of(USER, OFFICE)));

        final SickNote sickNote = sickNoteService.save(SickNote.builder()
            .person(person)
            .sickNoteType(sickNoteTypeService.getSickNoteTypes().getFirst())
            .startDate(today.minusDays(2))
            .endDate(today.minusDays(2))
            .status(ACTIVE)
            .build());

        sut.submitSickNoteExtension(person, sickNote.getId(), today.plusDays(1));

        when(commentService.create(any(), eq(EXTENSION_ACCEPTED), any(), any())).thenThrow(new IllegalStateException("comment could not be saved"));

        assertThatThrownBy(() -> sut.acceptSubmittedExtension(officeWithRoles, sickNote.getId(), "comment"))
            .isInstanceOf(IllegalStateException.class);

        final SickNote reloaded = sickNoteService.getById(sickNote.getId()).orElseThrow();
        assertThat(reloaded.getEndDate()).isEqualTo(today.minusDays(2));
        assertThat(sickNoteExtensionService.findSubmittedExtensionOfSickNote(reloaded)).isPresent();
    }
}
