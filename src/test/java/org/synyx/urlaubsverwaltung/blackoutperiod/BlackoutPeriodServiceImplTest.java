package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;

@ExtendWith(MockitoExtension.class)
class BlackoutPeriodServiceImplTest {

    private BlackoutPeriodServiceImpl sut;

    @Mock
    private BlackoutPeriodRepository blackoutPeriodRepository;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private PersonService personService;
    @Mock
    private ApplicationService applicationService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        sut = new BlackoutPeriodServiceImpl(blackoutPeriodRepository, departmentService, vacationTypeService, personService, applicationService, clock);
    }

    @Test
    void create_persistsEntityWithGeneratedTimestamps() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setTitle("Jahresabschluss");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));

        final BlackoutPeriodEntity savedEntity = new BlackoutPeriodEntity();
        savedEntity.setId(1L);
        savedEntity.setTitle("Jahresabschluss");
        savedEntity.setStartDate(LocalDate.of(2026, 12, 20));
        savedEntity.setEndDate(LocalDate.of(2027, 1, 5));
        when(blackoutPeriodRepository.save(any(BlackoutPeriodEntity.class))).thenReturn(savedEntity);

        final BlackoutPeriod createdBlackoutPeriod = sut.create(blackoutPeriod);

        final ArgumentCaptor<BlackoutPeriodEntity> captor = ArgumentCaptor.forClass(BlackoutPeriodEntity.class);
        verify(blackoutPeriodRepository).save(captor.capture());

        assertThat(captor.getValue().getCreatedAt()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(captor.getValue().getLastModification()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(createdBlackoutPeriod.getId()).isEqualTo(1L);
        assertThat(createdBlackoutPeriod.getTitle()).isEqualTo("Jahresabschluss");
    }

    @Test
    void update_throwsWhenBlackoutPeriodDoesNotExist() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);

        when(blackoutPeriodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatIllegalStateException().isThrownBy(() -> sut.update(blackoutPeriod));
    }

    @Test
    void findBlockingBlackoutPeriod_returnsEmptyWhenPeriodsDoNotOverlap() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Jahresabschluss");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        when(blackoutPeriodRepository.findAll()).thenReturn(List.of(entity));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), vacationType);

        assertThat(blockingBlackoutPeriod).isEmpty();
    }

    @Test
    void findBlockingBlackoutPeriod_returnsPeriodWhenCompanyWideAndOverlapping() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Jahresabschluss");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        when(blackoutPeriodRepository.findAll()).thenReturn(List.of(entity));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), vacationType);

        assertThat(blockingBlackoutPeriod).isPresent();
        assertThat(blockingBlackoutPeriod.get().getTitle()).isEqualTo("Jahresabschluss");
    }

    @Test
    void findBlockingBlackoutPeriod_returnsEmptyWhenPersonNotInScopedDepartment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Department scopedDepartment = createDepartment("Vertrieb");
        scopedDepartment.setId(42L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(scopedDepartment));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Vertriebssperre");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setDepartmentIds(Set.of(42L));
        when(blackoutPeriodRepository.findAll()).thenReturn(List.of(entity));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), vacationType);

        assertThat(blockingBlackoutPeriod).isEmpty();
    }

    @Test
    void findBlockingBlackoutPeriod_returnsPeriodWhenPersonIsInScopedDepartment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Department scopedDepartment = createDepartment("Vertrieb");
        scopedDepartment.setId(42L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(scopedDepartment));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(scopedDepartment));

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Vertriebssperre");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setDepartmentIds(Set.of(42L));
        when(blackoutPeriodRepository.findAll()).thenReturn(List.of(entity));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), vacationType);

        assertThat(blockingBlackoutPeriod).isPresent();
    }

    @Test
    void findBlockingBlackoutPeriod_returnsEmptyWhenVacationTypeNotRestricted() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());

        final VacationType<?> restrictedVacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(restrictedVacationType));

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Jahresabschluss");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setVacationTypeIds(Set.of(1L));
        when(blackoutPeriodRepository.findAll()).thenReturn(List.of(entity));

        final VacationType<?> requestedVacationType = createVacationType(2L, SPECIALLEAVE, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), requestedVacationType);

        assertThat(blockingBlackoutPeriod).isEmpty();
    }

    @Test
    void findBlackoutPeriodsForPerson_returnsOnlyPeriodsOverlappingAndApplicableToPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Department scopedDepartment = createDepartment("Vertrieb");
        scopedDepartment.setId(42L);
        final Department otherDepartment = createDepartment("Marketing");
        otherDepartment.setId(43L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(scopedDepartment, otherDepartment));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(scopedDepartment));

        final BlackoutPeriodEntity companyWide = new BlackoutPeriodEntity();
        companyWide.setId(1L);
        companyWide.setTitle("Jahresabschluss");
        companyWide.setStartDate(LocalDate.of(2026, 12, 20));
        companyWide.setEndDate(LocalDate.of(2027, 1, 5));

        final BlackoutPeriodEntity scoped = new BlackoutPeriodEntity();
        scoped.setId(2L);
        scoped.setTitle("Vertriebssperre");
        scoped.setStartDate(LocalDate.of(2026, 6, 1));
        scoped.setEndDate(LocalDate.of(2026, 6, 10));
        scoped.setDepartmentIds(Set.of(42L));

        final BlackoutPeriodEntity otherDepartmentOnly = new BlackoutPeriodEntity();
        otherDepartmentOnly.setId(3L);
        otherDepartmentOnly.setTitle("Marketingsperre");
        otherDepartmentOnly.setStartDate(LocalDate.of(2026, 6, 1));
        otherDepartmentOnly.setEndDate(LocalDate.of(2026, 6, 10));
        otherDepartmentOnly.setDepartmentIds(Set.of(43L));

        final BlackoutPeriodEntity outOfRange = new BlackoutPeriodEntity();
        outOfRange.setId(4L);
        outOfRange.setTitle("Nicht relevant");
        outOfRange.setStartDate(LocalDate.of(2025, 1, 1));
        outOfRange.setEndDate(LocalDate.of(2025, 1, 10));

        when(blackoutPeriodRepository.findAll()).thenReturn(List.of(companyWide, scoped, otherDepartmentOnly, outOfRange));

        final List<BlackoutPeriod> result = sut.findBlackoutPeriodsForPerson(person, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(result).extracting(BlackoutPeriod::getTitle).containsExactly("Vertriebssperre", "Jahresabschluss");
    }

    @Test
    void findConflictingApplications_queriesAllActivePersonsWhenCompanyWide() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Application application = new Application();
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2026, 12, 22));
        application.setEndDate(LocalDate.of(2026, 12, 23));
        application.setVacationType(createVacationType(1L, HOLIDAY, new StaticMessageSource()));

        when(applicationService.getApplicationsForACertainPeriodAndStatus(
            eq(LocalDate.of(2026, 12, 20)), eq(LocalDate.of(2027, 1, 5)), eq(List.of(person)), eq(activeStatuses())))
            .thenReturn(List.of(application));

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));

        final List<Application> conflicts = sut.findConflictingApplications(blackoutPeriod);

        assertThat(conflicts).containsExactly(application);
    }

    @Test
    void findConflictingApplications_queriesOnlyScopedDepartmentMembersWhenDepartmentRestricted() {

        final Person member = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Department scopedDepartment = createDepartment("Vertrieb");
        scopedDepartment.setMembers(List.of(member));

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));
        blackoutPeriod.setDepartments(List.of(scopedDepartment));

        when(applicationService.getApplicationsForACertainPeriodAndStatus(
            eq(LocalDate.of(2026, 12, 20)), eq(LocalDate.of(2027, 1, 5)), eq(List.of(member)), eq(activeStatuses())))
            .thenReturn(List.of());

        final List<Application> conflicts = sut.findConflictingApplications(blackoutPeriod);

        assertThat(conflicts).isEmpty();
        verify(personService, never()).getActivePersons();
    }
}
