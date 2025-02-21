package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class PersonServiceIT extends SingleTenantTestContainersBase {

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
    @Autowired
    private VacationTypeService vacationTypeService;

    @Test
    void deletePerson() {

        final LocalDate now = LocalDate.now();
        final VacationType<?> vacationType = vacationTypeService.getActiveVacationTypes().get(0);

        final Person personWithId = personService.create("user", "Marlene", "Muster", "muster@example.org", List.of(MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED), List.of(USER));
        final Long personId = personWithId.getId();

        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(personId), "42", "lala");
        personBasedataService.update(personBasedata);

        final Application application = new Application();
        application.setPerson(personWithId);
        application.setVacationType(vacationType);
        final Application applicationWithId = applicationService.save(application);

        final Application applicationWithCommentOfPerson = new Application();
        applicationWithCommentOfPerson.setVacationType(vacationType);
        final Application applicationWithCommentOfPersonWithId = applicationService.save(applicationWithCommentOfPerson);
        applicationCommentService.create(applicationWithCommentOfPersonWithId, ApplicationCommentAction.EDITED, Optional.of("Test"), personWithId);

        final Application applicationWithHolidayReplacementOfPerson = new Application();
        applicationWithHolidayReplacementOfPerson.setVacationType(vacationType);
        final HolidayReplacementEntity replacement = new HolidayReplacementEntity();
        replacement.setPerson(personWithId);
        applicationWithHolidayReplacementOfPerson.setHolidayReplacements(new ArrayList<>(List.of(replacement)));
        final Application applicationWithHolidayReplacementOfPersonWithId = applicationService.save(applicationWithHolidayReplacementOfPerson);

        final Application applicationWithCanceller = new Application();
        applicationWithCanceller.setCanceller(personWithId);
        applicationWithCanceller.setVacationType(vacationType);
        final Application applicationWithCancellerWithId = applicationService.save(applicationWithCanceller);

        final Application applicationWithBoss = new Application();
        applicationWithBoss.setBoss(personWithId);
        applicationWithBoss.setVacationType(vacationType);
        final Application applicationWithBossWithId = applicationService.save(applicationWithBoss);

        final Application applicationWithApplier = new Application();
        applicationWithApplier.setApplier(personWithId);
        applicationWithApplier.setVacationType(vacationType);
        final Application applicationWithApplierWithId = applicationService.save(applicationWithApplier);

        final SickNote sickNoteWithId =
            sickNoteService.save(SickNote.builder().person(personWithId).startDate(now.minusDays(5)).endDate(now.minusDays(3)).dayLength(DayLength.FULL).build());

        final SickNote sickNoteWithCommentWithId =
            sickNoteService.save(SickNote.builder().startDate(now.minusDays(1)).endDate(now.minusDays(1)).dayLength(DayLength.FULL).build());

        sickNoteCommentService.create(sickNoteWithCommentWithId, SickNoteCommentAction.COMMENTED, personWithId, "Test");

        final Overtime overtimeRecord = overtimeService.save(new Overtime(personWithId, now, now, Duration.ZERO), Optional.empty(), personWithId);

        final Department department = new Department();
        department.setName("department");
        department.setMembers(new ArrayList<>(List.of(personWithId)));
        department.setDepartmentHeads(new ArrayList<>(List.of(personWithId)));
        department.setSecondStageAuthorities(new ArrayList<>(List.of(personWithId)));
        final Department departmentWithId = departmentService.create(department);

        assertThat(personRepository.existsById(personId)).isTrue();
        assertThat(personRepository.findByUsernameIgnoreCase("user").get().getPermissions()).containsExactly(USER);
        assertThat(personBasedataService.getBasedataByPersonId(personId).get().personId().value()).isEqualTo(personId);
        assertThat(personRepository.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(OFFICE, MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED)).hasSize(1);
        assertThat(personRepository.countByPermissionsContainingAndIdNotIn(USER, List.of(personId + 1))).isOne();
        assertThat(applicationService.getApplicationById(applicationWithId.getId())).hasValue(applicationWithId);
        assertThat(applicationCommentService.getCommentsByApplication(applicationWithCommentOfPersonWithId)).hasSize(1);
        assertThat(applicationService.getApplicationById(applicationWithHolidayReplacementOfPersonWithId.getId()).get().getHolidayReplacements()).hasSize(1);
        assertThat(applicationService.getApplicationById(applicationWithCancellerWithId.getId()).get().getCanceller()).isEqualTo(personWithId);
        assertThat(applicationService.getApplicationById(applicationWithBossWithId.getId()).get().getBoss()).isEqualTo(personWithId);
        assertThat(applicationService.getApplicationById(applicationWithApplierWithId.getId()).get().getApplier()).isEqualTo(personWithId);
        assertThat(sickNoteService.getById(sickNoteWithId.getId())).hasValue(sickNoteWithId);
        assertThat(sickNoteCommentService.getCommentsBySickNote(sickNoteWithCommentWithId)).hasSize(1);
        assertThat(overtimeService.getOvertimeById(overtimeRecord.getId())).hasValue(overtimeRecord);
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getMembers()).hasSize(1);
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getDepartmentHeads()).hasSize(1);
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getSecondStageAuthorities()).hasSize(1);

        personService.delete(personWithId, new Person());

        assertThat(personRepository.existsById(personId)).isFalse();
        assertThat(personRepository.countByPermissionsContainingAndIdNotIn(USER, List.of(personId + 1))).isZero();
        assertThat(personRepository.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(OFFICE, MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED)).isEmpty();
        assertThat(personBasedataService.getBasedataByPersonId(personId)).isEmpty();
        assertThat(applicationService.getApplicationById(applicationWithId.getId())).isEmpty();
        assertThat(applicationCommentService.getCommentsByApplication(applicationWithCommentOfPersonWithId).get(0).person()).isNull();
        assertThat(applicationService.getApplicationById(applicationWithHolidayReplacementOfPersonWithId.getId()).get().getHolidayReplacements()).isEmpty();
        assertThat(applicationService.getApplicationById(applicationWithCancellerWithId.getId()).get().getCanceller()).isNull();
        assertThat(applicationService.getApplicationById(applicationWithBossWithId.getId()).get().getBoss()).isNull();
        assertThat(applicationService.getApplicationById(applicationWithApplierWithId.getId()).get().getApplier()).isNull();
        assertThat(sickNoteService.getById(sickNoteWithId.getId())).isEmpty();
        assertThat(sickNoteCommentService.getCommentsBySickNote(sickNoteWithCommentWithId).get(0).getPerson()).isNull();
        assertThat(overtimeService.getOvertimeById(overtimeRecord.getId())).isEmpty();
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getMembers()).isEmpty();
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getDepartmentHeads()).isEmpty();
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getSecondStageAuthorities()).isEmpty();
    }
}
