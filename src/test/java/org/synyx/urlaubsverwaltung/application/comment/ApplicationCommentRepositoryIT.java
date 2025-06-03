package org.synyx.urlaubsverwaltung.application.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.ALLOWED_DIRECTLY;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.COMMENTED;


@SpringBootTest
@Transactional
class ApplicationCommentRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private ApplicationCommentRepository sut;

    @Autowired
    private PersonService personService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ApplicationCommentService applicationCommentService;
    @Autowired
    private VacationTypeService vacationTypeService;

    @Test
    void ensureDeleteByApplicationPerson() {

        final VacationType<?> vacationType = vacationTypeService.getActiveVacationTypes().getFirst();

        final Person person = personService.create("batman", "Bruce", "Wayne", "batman@example.org");
        final Application application = new Application();
        application.setPerson(person);
        application.setVacationType(vacationType);

        final Person personToDelete = personService.create("robin", "Grayson", "Dick", "robin@example.org");
        final Application applicationToDelete = new Application();
        applicationToDelete.setPerson(personToDelete);
        applicationToDelete.setVacationType(vacationType);

        final Application applicationWithId = applicationService.save(application);
        final Application applicationToDeleteWithId = applicationService.save(applicationToDelete);

        applicationCommentService.create(applicationToDeleteWithId, ALLOWED_DIRECTLY, Optional.empty(), person);
        applicationCommentService.create(applicationWithId, ALLOWED, Optional.of("Whoop"), person);

        assertThat(sut.findAll()).hasSize(2);

        sut.deleteByApplicationPerson(personToDelete);

        final Iterable<ApplicationCommentEntity> commentsAfterDelete = sut.findAll();
        assertThat(commentsAfterDelete).hasSize(1);
        assertThat(commentsAfterDelete.iterator().next()).satisfies(commentEntity -> {
            assertThat(commentEntity.getApplicationId()).isEqualTo(applicationWithId.getId());
            assertThat(commentEntity.getPerson()).isSameAs(person);
            assertThat(commentEntity.getText()).isEqualTo("Whoop");
        });
    }

    @Test
    void ensureToGetApplicationCommentsInCorrectOrder() {

        final VacationType<?> vacationType = vacationTypeService.getActiveVacationTypes().getFirst();

        final Person person = personService.create("batman", "Bruce", "Wayne", "batman@example.org");

        final Application application = new Application();
        application.setPerson(person);
        application.setVacationType(vacationType);
        final Application savedApplication = applicationService.save(application);

        final ApplicationComment first = applicationCommentService.create(savedApplication, COMMENTED,  Optional.of("first"), person);
        final ApplicationComment second = applicationCommentService.create(savedApplication, COMMENTED,  Optional.of("second"), person);
        final ApplicationComment third = applicationCommentService.create(savedApplication, COMMENTED,  Optional.of("third"), person);
        final ApplicationComment fourth = applicationCommentService.create(savedApplication, COMMENTED,  Optional.of("fourth"), person);

        final List<ApplicationCommentEntity> applicationComments = sut.findByApplicationIdOrderByIdDesc(savedApplication.getId());
        assertThat(applicationComments)
            .extracting(ApplicationCommentEntity::getId)
            .containsExactly(fourth.id(), third.id(), second.id(), first.id());
    }
}
