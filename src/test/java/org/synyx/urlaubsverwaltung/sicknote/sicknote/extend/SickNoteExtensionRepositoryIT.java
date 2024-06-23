package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;

import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class SickNoteExtensionRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private SickNoteExtensionRepository sut;

    @Autowired
    private SickNoteService sickNoteService;
    @Autowired
    private SickNoteExtensionInteractionService sickNoteExtensionInteractionService;
    @Autowired
    private PersonService personService;

    @Test
    void ensureFindAllBySickNoteIdOrderByCreatedAt() {

        final LocalDate now = LocalDate.now(UTC);

        final Person person = personService.create("batman", "Bruce", "Wayne", "batman@example.org");
        person.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNoteToSave = sickNoteService.save(SickNote.builder().person(person).startDate(now.minusDays(10)).endDate(now.minusDays(10)).status(SickNoteStatus.ACTIVE).build());
        final SickNote sickNote = sickNoteService.save(sickNoteToSave);

        final Long sickNoteId = sickNote.getId();

        sickNoteExtensionInteractionService.submitSickNoteExtension(person, sickNoteId, now.plusDays(1));
        sickNoteExtensionInteractionService.acceptSubmittedExtension(person, sickNoteId, "");

        sickNoteExtensionInteractionService.submitSickNoteExtension(person, sickNoteId, now.plusDays(2));

        final List<SickNoteExtensionEntity> actual = sut.findAllBySickNoteIdOrderByCreatedAtDesc(sickNoteId);

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getNewEndDate()).isEqualTo(now.plusDays(2));
        assertThat(actual.get(1).getNewEndDate()).isEqualTo(now.plusDays(1));
    }
}
