package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.Month.MARCH;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createWorkingTime;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.FULLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.NO_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.person.Role.ADMIN;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Unit test for {@link SickNoteValidator}.
 */
@ExtendWith(MockitoExtension.class)
class SickNoteValidatorTest {

    private SickNoteValidator sut;

    @Mock
    private OverlapService overlapService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new SickNoteValidator(overlapService, workingTimeService, departmentService, Clock.systemUTC());
    }

    @Test
    void ensureNoApplierReturnsErrorOnEdit() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isOne();
    }

    @Test
    void ensureApplierWithWrongRoleReturnsError() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("dh", "department", "head", "department@example.org");
        applier.setPermissions(List.of(USER, ADMIN));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isOne();
    }

    @Test
    void ensureValidOfficeApplierHasNoErrors() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"SICK_NOTE_ADD", "SICK_NOTE_EDIT"})
    void ensureBossApplierWithValidPermissionsHasNoErrors(final Role role) {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("boss", "boss", "boss", "boss@example.org");
        applier.setPermissions(List.of(USER, BOSS, role));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureBossApplierWithInvalidPermissionsHasErrors() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("boss", "boss", "boss", "boss@example.org");
        applier.setPermissions(List.of(USER, BOSS));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isOne();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"SICK_NOTE_ADD", "SICK_NOTE_EDIT"})
    void ensureDepartmentHeadWithValidPermissionsHasNoErrors(final Role role) {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("dh", "department", "head", "department@example.org");
        applier.setPermissions(List.of(USER, DEPARTMENT_HEAD, role));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        when(departmentService.isDepartmentHeadAllowedToManagePerson(applier, person)).thenReturn(true);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"SICK_NOTE_ADD", "SICK_NOTE_EDIT"})
    void ensureDepartmentHeadWithValidPermissionsButForWrongMemberHasError(final Role role) {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("dh", "department", "head", "department@example.org");
        applier.setPermissions(List.of(USER, DEPARTMENT_HEAD, role));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        when(departmentService.isDepartmentHeadAllowedToManagePerson(applier, person)).thenReturn(false);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isOne();
    }

    @Test
    void ensureDepartmentHeadWithInvalidPermissionsHasError() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("dh", "department", "head", "department@example.org");
        applier.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isOne();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"SICK_NOTE_ADD", "SICK_NOTE_EDIT"})
    void ensureSecondStageAuthorityWithValidPermissionsHasNoErrors(final Role role) {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("ssa", "second stage authority", "second stage authority", "ssa@example.org");
        applier.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, role));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(applier, person)).thenReturn(true);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"SICK_NOTE_ADD", "SICK_NOTE_EDIT"})
    void ensureSecondStageAuthorityWithValidPermissionsButForWrongMemberHasError(final Role role) {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("ssa", "ssa", "ssa", "ssa@example.org");
        applier.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, role));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(applier, person)).thenReturn(false);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isOne();
    }

    @Test
    void ensureSecondStageAuthorityWithInvalidPermissionsHasError() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person applier = new Person("ssa", "ssa", "ssa", "ssa@example.org");
        applier.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isOne();
    }

    @Test
    void ensureValidDatesHaveNoErrors() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureDayLengthMayNotBeNull() {

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(null)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("dayLength").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureStartDateMayNotBeNull() {

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(null)
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("startDate").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureEndDateMayNotBeNull() {

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(null)
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriod() {

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, DECEMBER, 10))
            .endDate(LocalDate.of(2013, DECEMBER, 1))
            .dayLength(FULL)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");

        verifyNoInteractions(overlapService, workingTimeService);
    }

    @Test
    void ensureStartAndEndDateMustBeEqualsDatesForDayLengthNoon() {

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 21))
            .dayLength(NOON)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("sicknote.error.halfDayPeriod");
    }

    @Test
    void ensureStartAndEndDateMustBeEqualsDatesForDayLengthMorning() {

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 19))
            .endDate(LocalDate.of(2013, NOVEMBER, 21))
            .dayLength(MORNING)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("sicknote.error.halfDayPeriod");
    }

    @Test
    void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthMorning() {

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 21))
            .endDate(LocalDate.of(2013, NOVEMBER, 19))
            .dayLength(MORNING)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthNoon() {

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 21))
            .endDate(LocalDate.of(2013, NOVEMBER, 19))
            .dayLength(NOON)
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    void ensureAUStartDateMustBeBeforeAUEndDateToHaveAValidPeriod() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 1))
            .endDate(LocalDate.of(2013, NOVEMBER, 30))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 20))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 19))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubEndDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    void ensureValidAUPeriodHasNoErrors() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 1))
            .endDate(LocalDate.of(2013, NOVEMBER, 30))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 19))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 20))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDays() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 1))
            .endDate(LocalDate.of(2013, NOVEMBER, 30))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 1))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 30))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysStart() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 1))
            .endDate(LocalDate.of(2013, NOVEMBER, 30))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 1))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 10))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysEnd() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 1))
            .endDate(LocalDate.of(2013, NOVEMBER, 30))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 20))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 30))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysStartOverlapping() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 20))
            .endDate(LocalDate.of(2013, NOVEMBER, 30))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 1))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 20))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubStartDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysEndOverlapping() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 1))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 20))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 30))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubEndDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysNoneOverlapping() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 10))
            .endDate(LocalDate.of(2013, NOVEMBER, 20))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 1))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 9))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubStartDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
        assertThat(errors.getFieldErrors("aubEndDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodOneDay() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 1))
            .endDate(LocalDate.of(2013, NOVEMBER, 1))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 1))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 1))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodButIsNotForOneDay() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 1))
            .endDate(LocalDate.of(2013, NOVEMBER, 1))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 2))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 2))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubStartDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
    }

    @Test
    void ensureSickNoteMustNotHaveAnyOverlapping() {

        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, MARCH, 1))
            .endDate(LocalDate.of(2013, MARCH, 10))
            .dayLength(FULL)
            .build();

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(FULLY_OVERLAPPING);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getGlobalErrors().get(0).getCode()).isEqualTo("application.error.overlap");
    }

    @Test
    void ensureWorkingTimeConfigurationMustExistForPeriodOfSickNote() {
        final LocalDate startDate = LocalDate.of(2015, MARCH, 1);

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(startDate)
            .endDate(LocalDate.of(2015, MARCH, 10))
            .dayLength(FULL)
            .build();

        when(workingTimeService.getWorkingTime(any(Person.class), any(LocalDate.class))).thenReturn(Optional.empty());

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getGlobalErrors().get(0).getCode()).isEqualTo("sicknote.error.noValidWorkingTime");
        verify(workingTimeService).getWorkingTime(sickNote.getPerson(), startDate);
    }

    @Test
    void ensureInvalidPeriodWithValidAUBPeriodIsNotValid() {

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 10))
            .endDate(LocalDate.of(2013, NOVEMBER, 4))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 1))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 2))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    void ensureInvalidAUBPeriodWithValidPeriodIsNotValid() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final Person applier = new Person("office", "office", "office", "office@example.org");
        applier.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder()
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .applier(applier)
            .startDate(LocalDate.of(2013, NOVEMBER, 1))
            .endDate(LocalDate.of(2013, NOVEMBER, 4))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2013, NOVEMBER, 2))
            .aubEndDate(LocalDate.of(2013, NOVEMBER, 1))
            .build();

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubEndDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }
}
