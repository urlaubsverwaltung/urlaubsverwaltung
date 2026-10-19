package org.synyx.urlaubsverwaltung.person;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.absence.DateRange;
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
import org.synyx.urlaubsverwaltung.user.Theme;
import org.synyx.urlaubsverwaltung.user.UserSettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;
    @Autowired
    private UserSettingsService userSettingsService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureUpdateKeepsCreatedAtOfPersistedPerson() {

        final Person person = personService.create("user", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER));
        final Long personId = person.getId();

        // detach from the persistence context, so that the following update behaves like a separate request would
        entityManager.flush();
        entityManager.clear();

        final Instant createdAt = personService.getPersonByID(personId).orElseThrow().getCreatedAt();

        personService.update(new PersonId(personId),
            PersonUpdate.ofPersonalData("user", "Marlene", "Muster", "new-mail@example.org"));

        entityManager.flush();
        entityManager.clear();

        final Person updatedPerson = personService.getPersonByID(personId).orElseThrow();
        assertThat(updatedPerson.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updatedPerson.getEmail()).isEqualTo("new-mail@example.org");
    }

    @Test
    void ensureUpdateWithImmutableCollectionsDirectlyAfterCreation() {

        final Person person = personService.create("user", "Marlene", "Muster", "muster@example.org", List.of(), List.of());

        // callers hand over immutable collections, e.g. Stream#toList, and the person is updated within the
        // very transaction it was created in, like the demo data creation does
        final Person updatedPerson = personService.update(person.getIdAsPersonId(),
            PersonUpdate.ofPermissions(List.of(USER))
                .withNotifications(List.of(MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED)));

        assertThat(updatedPerson.getPermissions()).containsExactly(USER);
        assertThat(updatedPerson.getNotifications()).containsExactly(MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
    }

    @Test
    void deletePerson() {

        final LocalDate now = LocalDate.now();
        final VacationType<?> vacationType = vacationTypeService.getActiveVacationTypes().getFirst();

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

        final Overtime overtimeRecord = overtimeService.createOvertime(personWithId.getIdAsPersonId(), new DateRange(now, now), Duration.ZERO, personWithId.getIdAsPersonId(), "");

        final Department department = new Department();
        department.setName("department");
        department.setMembers(new ArrayList<>(List.of(personWithId)));
        department.setDepartmentHeads(new ArrayList<>(List.of(personWithId)));
        department.setSecondStageAuthorities(new ArrayList<>(List.of(personWithId)));
        final Department departmentWithId = departmentService.create(department);

        assertThat(personRepository.existsById(personId)).isTrue();
        assertThat(personRepository.findByUsernameIgnoreCase("user").get().getPermissions()).containsExactly(USER);
        assertThat(personBasedataService.getBasedataByPersonId(personId).get().personId().value()).isEqualTo(personId);
        assertThat(personRepository.findByPermissionsContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(USER, MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED)).hasSize(1);
        assertThat(personRepository.countByPermissionsContainingAndIdNotIn(USER, List.of(personId + 1))).isOne();
        assertThat(applicationService.getApplicationById(applicationWithId.getId())).hasValue(applicationWithId);
        assertThat(applicationCommentService.getCommentsByApplication(applicationWithCommentOfPersonWithId)).hasSize(1);
        assertThat(applicationService.getApplicationById(applicationWithHolidayReplacementOfPersonWithId.getId()).get().getHolidayReplacements()).hasSize(1);
        assertThat(applicationService.getApplicationById(applicationWithCancellerWithId.getId()).get().getCanceller()).isEqualTo(personWithId);
        assertThat(applicationService.getApplicationById(applicationWithBossWithId.getId()).get().getBoss()).isEqualTo(personWithId);
        assertThat(applicationService.getApplicationById(applicationWithApplierWithId.getId()).get().getApplier()).isEqualTo(personWithId);
        assertThat(sickNoteService.getById(sickNoteWithId.getId())).hasValue(sickNoteWithId);
        assertThat(sickNoteCommentService.getCommentsBySickNote(sickNoteWithCommentWithId)).hasSize(1);
        assertThat(overtimeService.getOvertimeById(overtimeRecord.id())).hasValue(overtimeRecord);
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getMembers()).hasSize(1);
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getDepartmentHeads()).hasSize(1);
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getSecondStageAuthorities()).hasSize(1);

        personService.delete(personWithId.getIdAsPersonId(), new PersonId(personId + 1));

        assertThat(personRepository.existsById(personId)).isFalse();
        assertThat(personRepository.countByPermissionsContainingAndIdNotIn(USER, List.of(personId + 1))).isZero();
        assertThat(personRepository.findByPermissionsContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(USER, MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED)).isEmpty();
        assertThat(personBasedataService.getBasedataByPersonId(personId)).isEmpty();
        assertThat(applicationService.getApplicationById(applicationWithId.getId())).isEmpty();
        assertThat(applicationCommentService.getCommentsByApplication(applicationWithCommentOfPersonWithId).getFirst().person()).isNull();
        assertThat(applicationService.getApplicationById(applicationWithHolidayReplacementOfPersonWithId.getId()).get().getHolidayReplacements()).isEmpty();
        assertThat(applicationService.getApplicationById(applicationWithCancellerWithId.getId()).get().getCanceller()).isNull();
        assertThat(applicationService.getApplicationById(applicationWithBossWithId.getId()).get().getBoss()).isNull();
        assertThat(applicationService.getApplicationById(applicationWithApplierWithId.getId()).get().getApplier()).isNull();
        assertThat(sickNoteService.getById(sickNoteWithId.getId())).isEmpty();
        assertThat(sickNoteCommentService.getCommentsBySickNote(sickNoteWithCommentWithId).getFirst().getPerson()).isNull();
        assertThat(overtimeService.getOvertimeById(overtimeRecord.id())).isEmpty();
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getMembers()).isEmpty();
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getDepartmentHeads()).isEmpty();
        assertThat(departmentService.getDepartmentById(departmentWithId.getId()).get().getSecondStageAuthorities()).isEmpty();
    }

    /**
     * Deleting only the {@code person} row lets the database clean up everything referencing the person: data owned
     * by the person is deleted, references to the person on data of others are set to {@code null}.
     */
    @Test
    void ensureDeletingThePersonRowDeletesOwnedDataAndUnsetsReferencesOnDataOfOthers() {

        final LocalDate now = LocalDate.now();
        final VacationType<?> vacationType = vacationTypeService.getActiveVacationTypes().getFirst();

        final Person person = personService.create("user", "Marlene", "Muster", "muster@example.org", List.of(MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED), List.of(USER));
        final Long personId = person.getId();
        final Person other = personService.create("other", "Max", "Muster", "max@example.org", List.of(MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED), List.of(USER));

        // data owned by the person, the holidays account is created together with the person
        personBasedataService.update(new PersonBasedata(new PersonId(personId), "42", "lala"));
        workingTimeWriteService.touch(List.of(1, 2, 3, 4, 5), now.minusYears(1), person);
        userSettingsService.updateUserPreference(person, Theme.DARK, null);

        final Application application = applicationOf(person, vacationType);
        applicationCommentService.create(application, ApplicationCommentAction.EDITED, Optional.of("by other"), other);
        final Application applicationWithReplacement = applicationOf(person, vacationType);
        applicationWithReplacement.setHolidayReplacements(new ArrayList<>(List.of(holidayReplacement(other))));
        applicationService.save(applicationWithReplacement);

        // the person is both owner and boss of the same application, so the row is reached by CASCADE and SET NULL
        final Application selfApprovedApplication = applicationOf(person, vacationType);
        selfApprovedApplication.setBoss(person);
        selfApprovedApplication.setApplier(person);
        selfApprovedApplication.setCanceller(person);
        applicationService.save(selfApprovedApplication);

        final SickNote sickNote = sickNoteService.save(SickNote.builder().person(person).applier(person).startDate(now.minusDays(5)).endDate(now.minusDays(3)).dayLength(DayLength.FULL).build());
        sickNoteCommentService.create(sickNote, SickNoteCommentAction.COMMENTED, other, "by other");

        final Overtime overtime = overtimeService.createOvertime(person.getIdAsPersonId(), new DateRange(now, now), Duration.ofHours(1), other.getIdAsPersonId(), "by other");

        final Department department = new Department();
        department.setName("department");
        department.setMembers(new ArrayList<>(List.of(person)));
        department.setDepartmentHeads(new ArrayList<>(List.of(person)));
        department.setSecondStageAuthorities(new ArrayList<>(List.of(person)));
        final Department departmentWithId = departmentService.create(department);

        // data of others referencing the person
        final Application applicationOfOther = applicationOf(other, vacationType);
        applicationOfOther.setBoss(person);
        applicationOfOther.setApplier(person);
        applicationOfOther.setCanceller(person);
        applicationOfOther.setHolidayReplacements(new ArrayList<>(List.of(holidayReplacement(person), holidayReplacement(other))));
        applicationService.save(applicationOfOther);
        applicationCommentService.create(applicationOfOther, ApplicationCommentAction.EDITED, Optional.of("by person"), person);

        final SickNote sickNoteOfOther = sickNoteService.save(SickNote.builder().person(other).applier(person).startDate(now.minusDays(2)).endDate(now.minusDays(1)).dayLength(DayLength.FULL).build());
        sickNoteCommentService.create(sickNoteOfOther, SickNoteCommentAction.COMMENTED, person, "by person");

        final Overtime overtimeOfOther = overtimeService.createOvertime(other.getIdAsPersonId(), new DateRange(now, now), Duration.ofHours(1), person.getIdAsPersonId(), "by person");

        entityManager.flush();

        // rows that have no service API outside their package
        jdbcTemplate.update("INSERT INTO sick_note_extension (tenant_id, id, created_at, sick_note_id, new_end_date, is_aub, status) SELECT tenant_id, nextval('sick_note_extension_id_seq'), now(), id, end_date + 1, false, 'SUBMITTED' FROM sick_note WHERE id = ?", sickNote.getId());
        jdbcTemplate.update("INSERT INTO person_calendar (tenant_id, id, calendar_period, secret, person_id) SELECT tenant_id, nextval('person_calendar_id_seq'), 'P1Y', 'secret', id FROM person WHERE id = ?", personId);
        jdbcTemplate.update("INSERT INTO company_calendar (tenant_id, id, calendar_period, secret, person_id) SELECT tenant_id, nextval('company_calendar_id_seq'), 'P1Y', 'secret', id FROM person WHERE id = ?", personId);
        jdbcTemplate.update("INSERT INTO department_calendar (tenant_id, id, calendar_period, secret, person_id, department_id) SELECT tenant_id, nextval('department_calendar_id_seq'), 'P1Y', 'secret', id, ? FROM person WHERE id = ?", departmentWithId.getId(), personId);
        jdbcTemplate.update("INSERT INTO user_pagination_settings (tenant_id, person_id, default_page_size) SELECT tenant_id, id, 20 FROM person WHERE id = ?", personId);
        jdbcTemplate.update("INSERT INTO user_notification_settings (tenant_id, person_id, restrict_to_departments) SELECT tenant_id, id, false FROM person WHERE id = ?", personId);

        final List<String> ownedTables = List.of(
            "person_permissions", "person_notifications", "person_basedata", "person_active_period",
            "account", "working_time", "application", "sick_note", "overtime", "department_membership",
            "person_calendar", "company_calendar", "department_calendar",
            "user_settings", "user_pagination_settings", "user_notification_settings"
        );
        // guards the fixture: every table has a row of the person before the delete
        ownedTables.forEach(table -> assertThat(countByPerson(table, personId)).as(table).isPositive());
        assertThat(countOf("application_comment", "application_id", application.getId())).isOne();
        assertThat(countOf("holiday_replacements", "application_id", applicationWithReplacement.getId())).isOne();
        assertThat(countOf("sick_note_comment", "sick_note_id", sickNote.getId())).isPositive();
        assertThat(countOf("sick_note_extension", "sick_note_id", sickNote.getId())).isOne();
        assertThat(countOf("overtime_comment", "overtime_id", overtime.id().value())).isOne();

        entityManager.clear();
        jdbcTemplate.update("DELETE FROM person WHERE id = ?", personId);

        // data owned by the person is gone
        assertThat(countOf("person", "id", personId)).isZero();
        ownedTables.forEach(table -> assertThat(countByPerson(table, personId)).as(table).isZero());
        assertThat(countOf("application_comment", "application_id", application.getId())).isZero();
        assertThat(countOf("holiday_replacements", "application_id", applicationWithReplacement.getId())).isZero();
        assertThat(countOf("sick_note_comment", "sick_note_id", sickNote.getId())).isZero();
        assertThat(countOf("sick_note_extension", "sick_note_id", sickNote.getId())).isZero();
        assertThat(countOf("overtime_comment", "overtime_id", overtime.id().value())).isZero();

        // data of others is kept, the references to the person are unset
        assertThat(countOf("application", "id", applicationOfOther.getId())).isOne();
        assertThat(countOf("application", "boss_id", personId)).isZero();
        assertThat(countOf("application", "applier_id", personId)).isZero();
        assertThat(countOf("application", "canceller_id", personId)).isZero();
        assertThat(jdbcTemplate.queryForList("SELECT person_id FROM holiday_replacements WHERE application_id = ?", Long.class, applicationOfOther.getId()))
            .containsExactly(other.getId());
        assertThat(countOf("application_comment", "application_id", applicationOfOther.getId())).isPositive();
        assertThat(countByPerson("application_comment", personId)).isZero();

        assertThat(countOf("sick_note", "id", sickNoteOfOther.getId())).isOne();
        assertThat(countOf("sick_note", "applier_id", personId)).isZero();
        assertThat(countOf("sick_note_comment", "sick_note_id", sickNoteOfOther.getId())).isPositive();
        assertThat(countByPerson("sick_note_comment", personId)).isZero();

        assertThat(countOf("overtime", "id", overtimeOfOther.id().value())).isOne();
        assertThat(countOf("overtime_comment", "overtime_id", overtimeOfOther.id().value())).isOne();
        assertThat(countByPerson("overtime_comment", personId)).isZero();

        assertThat(countOf("department", "id", departmentWithId.getId())).isOne();
        assertThat(countOf("person", "id", other.getId())).isOne();
    }

    private Application applicationOf(Person person, VacationType<?> vacationType) {
        final Application application = new Application();
        application.setPerson(person);
        application.setVacationType(vacationType);
        return applicationService.save(application);
    }

    private static HolidayReplacementEntity holidayReplacement(Person person) {
        final HolidayReplacementEntity replacement = new HolidayReplacementEntity();
        replacement.setPerson(person);
        return replacement;
    }

    private long countByPerson(String table, Long personId) {
        return countOf(table, "person_id", personId);
    }

    private long countOf(String table, String column, Long value) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM " + table + " WHERE " + column + " = ?", Long.class, value);
    }
}
