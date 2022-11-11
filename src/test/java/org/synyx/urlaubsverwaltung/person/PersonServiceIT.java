package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentEntity;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class PersonServiceIT extends TestContainersBase {

    @Autowired
    private PersonService personService;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PersonBasedataService personBasedataService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ApplicationCommentService applicationCommentService;
    @Autowired
    private SickNoteService sickNoteService;
    @Autowired
    private SickNoteCommentService sickNoteCommentService;
    @Autowired
    private OvertimeService overtimeService;
    @Autowired
    private DepartmentService departmentService;

    @Test
    void deletePerson() {

        final Person person = new Person("user", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(MailNotification.NOTIFICATION_USER));
        final Person personWithId = personService.create(person);
        final Integer personId = personWithId.getId();

        PersonBasedata personBasedata = new PersonBasedata(new PersonId(personId), "42", "lala");
        personBasedataService.update(personBasedata);

        final Application application = new Application();
        application.setPerson(personWithId);
        final Application applicationWithId = applicationService.save(application);

        final Application applicationWithCommentOfPerson = new Application();
        final Application applicationWithCommentOfPersonWithId = applicationService.save(applicationWithCommentOfPerson);
        applicationCommentService.create(applicationWithCommentOfPersonWithId, ApplicationCommentAction.EDITED, Optional.of("Test"), personWithId);

        final SickNote sickNote = new SickNote();
        sickNote.setPerson(personWithId);
        final SickNote sickNoteWithId = sickNoteService.save(sickNote);

        final SickNote sickNoteWithComment = new SickNote();
        final SickNote sickNoteWithCommentWithId = sickNoteService.save(sickNoteWithComment);
        sickNoteCommentService.create(sickNoteWithCommentWithId, SickNoteCommentAction.COMMENTED, personWithId, "Test");

        final Overtime overtimeRecord = overtimeService.record(new Overtime(personWithId, LocalDate.now(), LocalDate.now(), Duration.ZERO), Optional.empty(), personWithId);

        final Department department = new Department();
        department.setName("department");
        department.setMembers(new ArrayList<>(List.of(personWithId)));
        department.setDepartmentHeads(new ArrayList<>(List.of(personWithId)));
        department.setSecondStageAuthorities(new ArrayList<>(List.of(personWithId)));
        final Department departmentWithId = departmentService.create(department);

        assertThat(personRepository.existsById(personId)).isTrue();
        assertThat(personRepository.findByUsername("user").get().getPermissions()).containsExactly(USER);
        assertThat(personBasedataService.getBasedataByPersonId(personId).get().getPersonId().getValue()).isEqualTo(personId);
        assertThat(personRepository.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(OFFICE, MailNotification.NOTIFICATION_USER)).hasSize(1);
        assertThat(personRepository.countByPermissionsContainingAndIdNotIn(USER, List.of(personId+1))).isOne();
        assertThat(applicationService.getApplicationById(applicationWithId.getId())).isEqualTo(Optional.of(applicationWithId));
        assertThat(applicationCommentService.getCommentsByApplication(applicationWithCommentOfPerson)).hasSize(1);
        assertThat(sickNoteService.getById(sickNoteWithId.getId())).hasValue(sickNoteWithId);
        assertThat(sickNoteCommentService.getCommentsBySickNote(sickNoteWithCommentWithId)).hasSize(1);
        assertThat(overtimeService.getOvertimeById(overtimeRecord.getId())).hasValue(overtimeRecord);
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getMembers()).hasSize(1);
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getDepartmentHeads()).hasSize(1);
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getSecondStageAuthorities()).hasSize(1);

        personService.delete(personWithId, new Person());

        assertThat(personRepository.existsById(personId)).isFalse();
        assertThat(personRepository.countByPermissionsContainingAndIdNotIn(USER, List.of(personId+1))).isZero();
        assertThat(personRepository.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(OFFICE, MailNotification.NOTIFICATION_USER)).isEmpty();
        assertThat(personBasedataService.getBasedataByPersonId(personId)).isEmpty();
        assertThat(applicationService.getApplicationById(applicationWithId.getId())).isEmpty();
        assertThat(applicationCommentService.getCommentsByApplication(applicationWithCommentOfPerson).get(0).getPerson()).isNull();
        assertThat(sickNoteService.getById(sickNoteWithId.getId())).isEmpty();
        assertThat(sickNoteCommentService.getCommentsBySickNote(sickNoteWithCommentWithId).get(0).getPerson()).isNull();
        assertThat(overtimeService.getOvertimeById(overtimeRecord.getId())).isEmpty();
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getMembers()).isEmpty();
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getDepartmentHeads()).isEmpty();
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getSecondStageAuthorities()).isEmpty();
    }
}
