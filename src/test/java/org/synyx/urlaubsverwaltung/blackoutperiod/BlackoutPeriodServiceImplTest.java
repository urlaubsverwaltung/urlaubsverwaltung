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
import org.synyx.urlaubsverwaltung.department.DepartmentDeletedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void findBlockingBlackoutPeriod_returnsEmptyWithoutDepartmentLookupWhenNothingOverlaps() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10))).thenReturn(List.of());

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), vacationType);

        assertThat(blockingBlackoutPeriod).isEmpty();
        verifyNoInteractions(departmentService, vacationTypeService);
    }

    @Test
    void findBlockingBlackoutPeriod_returnsPeriodWithoutDepartmentLookupWhenCompanyWideAndOverlapping() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Jahresabschluss");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setCompanyWide(true);
        entity.setAllVacationTypes(true);
        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23))).thenReturn(List.of(entity));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), vacationType);

        assertThat(blockingBlackoutPeriod).isPresent();
        assertThat(blockingBlackoutPeriod.get().getTitle()).isEqualTo("Jahresabschluss");
        verifyNoInteractions(departmentService);
    }

    @Test
    void findBlockingBlackoutPeriod_returnsEmptyWhenPersonNotInScopedDepartment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(departmentService.getDepartmentIdsByMembers(List.of(person))).thenReturn(Map.of(new PersonId(1L), Set.of(43L)));

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Vertriebssperre");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setDepartmentIds(Set.of(42L));
        entity.setAllVacationTypes(true);
        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23))).thenReturn(List.of(entity));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), vacationType);

        assertThat(blockingBlackoutPeriod).isEmpty();
        verify(departmentService, never()).getAllDepartments();
    }

    @Test
    void findBlockingBlackoutPeriod_returnsPeriodWhenPersonIsInScopedDepartment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(departmentService.getDepartmentIdsByMembers(List.of(person))).thenReturn(Map.of(new PersonId(1L), Set.of(42L)));

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Vertriebssperre");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setDepartmentIds(Set.of(42L));
        entity.setAllVacationTypes(true);
        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23))).thenReturn(List.of(entity));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), vacationType);

        assertThat(blockingBlackoutPeriod).isPresent();
        assertThat(blockingBlackoutPeriod.get().getTitle()).isEqualTo("Vertriebssperre");
        verify(departmentService, never()).getAllDepartments();
        verify(departmentService, never()).getAssignedDepartmentsOfMember(any());
    }

    @Test
    void findBlockingBlackoutPeriod_returnsEmptyWhenVacationTypeNotRestricted() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Jahresabschluss");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setCompanyWide(true);
        entity.setVacationTypeIds(Set.of(1L));
        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23))).thenReturn(List.of(entity));

        final VacationType<?> requestedVacationType = createVacationType(2L, SPECIALLEAVE, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), requestedVacationType);

        assertThat(blockingBlackoutPeriod).isEmpty();
        verifyNoInteractions(departmentService, vacationTypeService);
    }

    @Test
    void findBlackoutPeriodsForPerson_returnsOnlyPeriodsApplicableToPersonOrderedByStartDate() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(departmentService.getDepartmentIdsByMembers(List.of(person))).thenReturn(Map.of(new PersonId(1L), Set.of(42L)));

        final BlackoutPeriodEntity companyWide = new BlackoutPeriodEntity();
        companyWide.setId(1L);
        companyWide.setTitle("Jahresabschluss");
        companyWide.setStartDate(LocalDate.of(2026, 12, 20));
        companyWide.setEndDate(LocalDate.of(2027, 1, 5));
        companyWide.setCompanyWide(true);

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

        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
            .thenReturn(List.of(companyWide, scoped, otherDepartmentOnly));

        final List<BlackoutPeriod> result = sut.findBlackoutPeriodsForPerson(person, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(result).extracting(BlackoutPeriod::getTitle).containsExactly("Vertriebssperre", "Jahresabschluss");
        verify(departmentService, never()).getAllDepartments();
    }

    @Test
    void findBlackoutPeriodsForPersons_resolvesDepartmentsOfAllPersonsAtOnce() {

        final Person sales = new Person("sales", "Sales", "Sam", "sales@example.org");
        sales.setId(1L);
        final Person marketing = new Person("marketing", "Marketing", "Mia", "marketing@example.org");
        marketing.setId(2L);

        when(departmentService.getDepartmentIdsByMembers(List.of(sales, marketing)))
            .thenReturn(Map.of(new PersonId(1L), Set.of(42L), new PersonId(2L), Set.of(43L)));

        final BlackoutPeriodEntity companyWide = new BlackoutPeriodEntity();
        companyWide.setId(1L);
        companyWide.setTitle("Jahresabschluss");
        companyWide.setStartDate(LocalDate.of(2026, 12, 20));
        companyWide.setEndDate(LocalDate.of(2027, 1, 5));
        companyWide.setCompanyWide(true);
        companyWide.setAllVacationTypes(true);

        final BlackoutPeriodEntity salesOnly = new BlackoutPeriodEntity();
        salesOnly.setId(2L);
        salesOnly.setTitle("Vertriebssperre");
        salesOnly.setStartDate(LocalDate.of(2026, 6, 1));
        salesOnly.setEndDate(LocalDate.of(2026, 6, 10));
        salesOnly.setDepartmentIds(Set.of(42L));
        salesOnly.setAllVacationTypes(true);

        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
            .thenReturn(List.of(companyWide, salesOnly));

        final Map<PersonId, List<BlackoutPeriod>> result =
            sut.findBlackoutPeriodsForPersons(List.of(sales, marketing), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(result.get(new PersonId(1L))).extracting(BlackoutPeriod::getTitle).containsExactly("Vertriebssperre", "Jahresabschluss");
        assertThat(result.get(new PersonId(2L))).extracting(BlackoutPeriod::getTitle).containsExactly("Jahresabschluss");
        verify(departmentService, never()).getAssignedDepartmentsOfMember(any());
        verify(departmentService, never()).getAllDepartments();
    }

    @Test
    void findBlackoutPeriodsForPersons_doesNotLookUpDepartmentsWhenOnlyCompanyWideBlackoutPeriodsOverlap() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final BlackoutPeriodEntity companyWide = new BlackoutPeriodEntity();
        companyWide.setId(1L);
        companyWide.setTitle("Jahresabschluss");
        companyWide.setStartDate(LocalDate.of(2026, 12, 20));
        companyWide.setEndDate(LocalDate.of(2027, 1, 5));
        companyWide.setCompanyWide(true);
        companyWide.setAllVacationTypes(true);
        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))).thenReturn(List.of(companyWide));

        final Map<PersonId, List<BlackoutPeriod>> result =
            sut.findBlackoutPeriodsForPersons(List.of(person), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(result.get(new PersonId(1L))).extracting(BlackoutPeriod::getTitle).containsExactly("Jahresabschluss");
        verifyNoInteractions(departmentService, vacationTypeService);
    }

    @Test
    void findBlackoutPeriodsForPersons_resolvesVacationTypesOnceButNotTheDepartments() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(departmentService.getDepartmentIdsByMembers(List.of(person))).thenReturn(Map.of(new PersonId(1L), Set.of(42L)));

        final VacationType<?> holiday = createVacationType(1L, HOLIDAY, new StaticMessageSource());
        final VacationType<?> specialLeave = createVacationType(2L, SPECIALLEAVE, new StaticMessageSource());
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(holiday, specialLeave));

        final BlackoutPeriodEntity restrictedToHoliday = new BlackoutPeriodEntity();
        restrictedToHoliday.setId(1L);
        restrictedToHoliday.setTitle("Vertriebssperre");
        restrictedToHoliday.setStartDate(LocalDate.of(2026, 6, 1));
        restrictedToHoliday.setEndDate(LocalDate.of(2026, 6, 10));
        restrictedToHoliday.setDepartmentIds(Set.of(42L));
        restrictedToHoliday.setVacationTypeIds(Set.of(1L));

        final BlackoutPeriodEntity restrictedToSpecialLeave = new BlackoutPeriodEntity();
        restrictedToSpecialLeave.setId(2L);
        restrictedToSpecialLeave.setTitle("Jahresabschluss");
        restrictedToSpecialLeave.setStartDate(LocalDate.of(2026, 12, 20));
        restrictedToSpecialLeave.setEndDate(LocalDate.of(2027, 1, 5));
        restrictedToSpecialLeave.setCompanyWide(true);
        restrictedToSpecialLeave.setVacationTypeIds(Set.of(2L));

        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
            .thenReturn(List.of(restrictedToSpecialLeave, restrictedToHoliday));

        final List<BlackoutPeriod> result =
            sut.findBlackoutPeriodsForPersons(List.of(person), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)).get(new PersonId(1L));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVacationTypes()).containsExactly(holiday);
        assertThat(result.get(0).getDepartments()).isEmpty();
        assertThat(result.get(1).getVacationTypes()).containsExactly(specialLeave);
        verify(vacationTypeService).getAllVacationTypes();
        verify(departmentService, never()).getAllDepartments();
    }

    @Test
    void findBlackoutPeriodsForPersons_returnsEmptyListsWithoutLookingUpDepartmentsWhenNothingOverlaps() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))).thenReturn(List.of());

        final Map<PersonId, List<BlackoutPeriod>> result =
            sut.findBlackoutPeriodsForPersons(List.of(person), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(result).containsEntry(new PersonId(1L), List.of());
        verifyNoInteractions(departmentService, vacationTypeService);
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
        blackoutPeriod.setCompanyWide(true);
        blackoutPeriod.setAllVacationTypes(true);

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

    @Test
    void getAllBlackoutPeriods_returnsEmptyListWithoutResolvingDepartmentsAndVacationTypes() {

        when(blackoutPeriodRepository.findAll()).thenReturn(List.of());

        assertThat(sut.getAllBlackoutPeriods()).isEmpty();

        verifyNoInteractions(departmentService, vacationTypeService);
    }

    @Test
    void getBlackoutPeriodById_returnsMappedBlackoutPeriod() {

        final Department department = createDepartment("Vertrieb");
        department.setId(42L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Vertriebssperre");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setCreatedAt(LocalDate.of(2025, 1, 1));
        entity.setDepartmentIds(Set.of(42L));
        entity.setVacationTypeIds(Set.of(1L));
        when(blackoutPeriodRepository.findById(1L)).thenReturn(Optional.of(entity));

        final Optional<BlackoutPeriod> blackoutPeriod = sut.getBlackoutPeriodById(1L);

        assertThat(blackoutPeriod).isPresent();
        assertThat(blackoutPeriod.get().getTitle()).isEqualTo("Vertriebssperre");
        assertThat(blackoutPeriod.get().getCreatedAt()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(blackoutPeriod.get().getDepartments()).containsExactly(department);
        assertThat(blackoutPeriod.get().getVacationTypes()).containsExactly(vacationType);
    }

    @Test
    void getBlackoutPeriodById_returnsEmptyForUnknownId() {

        when(blackoutPeriodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(sut.getBlackoutPeriodById(1L)).isEmpty();
    }

    @Test
    void update_keepsCreatedAtAndUpdatesLastModification() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);
        blackoutPeriod.setTitle("Jahresabschluss (verlängert)");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 12));

        final BlackoutPeriodEntity existingEntity = new BlackoutPeriodEntity();
        existingEntity.setId(1L);
        existingEntity.setCreatedAt(LocalDate.of(2025, 5, 5));
        existingEntity.setLastModification(LocalDate.of(2025, 5, 5));
        when(blackoutPeriodRepository.findById(1L)).thenReturn(Optional.of(existingEntity));

        final BlackoutPeriodEntity savedEntity = new BlackoutPeriodEntity();
        savedEntity.setId(1L);
        savedEntity.setTitle("Jahresabschluss (verlängert)");
        savedEntity.setStartDate(LocalDate.of(2026, 12, 20));
        savedEntity.setEndDate(LocalDate.of(2027, 1, 12));
        when(blackoutPeriodRepository.save(any(BlackoutPeriodEntity.class))).thenReturn(savedEntity);

        final BlackoutPeriod updatedBlackoutPeriod = sut.update(blackoutPeriod);

        final ArgumentCaptor<BlackoutPeriodEntity> captor = ArgumentCaptor.forClass(BlackoutPeriodEntity.class);
        verify(blackoutPeriodRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(LocalDate.of(2025, 5, 5));
        assertThat(captor.getValue().getLastModification()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(updatedBlackoutPeriod.getTitle()).isEqualTo("Jahresabschluss (verlängert)");
        assertThat(updatedBlackoutPeriod.getEndDate()).isEqualTo(LocalDate.of(2027, 1, 12));
    }

    @Test
    void delete_delegatesToRepository() {

        sut.delete(1L);

        verify(blackoutPeriodRepository).deleteById(1L);
    }

    @Test
    void findBlockingBlackoutPeriod_returnsPeriodWithItsVacationTypesWhenVacationTypeIsRestricted() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final VacationType<?> restrictedVacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(restrictedVacationType));

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Jahresabschluss");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setCompanyWide(true);
        entity.setVacationTypeIds(Set.of(1L));
        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23))).thenReturn(List.of(entity));

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), restrictedVacationType);

        assertThat(blockingBlackoutPeriod).isPresent();
        assertThat(blockingBlackoutPeriod.get().getTitle()).isEqualTo("Jahresabschluss");
        assertThat(blockingBlackoutPeriod.get().getVacationTypes()).containsExactly(restrictedVacationType);
        verifyNoInteractions(departmentService);
    }

    @Test
    void findBlockingBlackoutPeriod_returnsEarliestOfMultipleMatchingPeriods() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final BlackoutPeriodEntity later = new BlackoutPeriodEntity();
        later.setId(1L);
        later.setTitle("Später");
        later.setStartDate(LocalDate.of(2026, 12, 23));
        later.setEndDate(LocalDate.of(2026, 12, 27));
        later.setCompanyWide(true);
        later.setAllVacationTypes(true);

        final BlackoutPeriodEntity earlier = new BlackoutPeriodEntity();
        earlier.setId(2L);
        earlier.setTitle("Früher");
        earlier.setStartDate(LocalDate.of(2026, 12, 20));
        earlier.setEndDate(LocalDate.of(2026, 12, 24));
        earlier.setCompanyWide(true);
        earlier.setAllVacationTypes(true);

        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 12, 23), LocalDate.of(2026, 12, 24))).thenReturn(List.of(later, earlier));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 23), LocalDate.of(2026, 12, 24), vacationType);

        assertThat(blockingBlackoutPeriod).isPresent();
        assertThat(blockingBlackoutPeriod.get().getTitle()).isEqualTo("Früher");
    }

    @Test
    void findConflictingApplications_returnsEmptyWhenNoPersonIsAffected() {

        when(personService.getActivePersons()).thenReturn(List.of());

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));
        blackoutPeriod.setCompanyWide(true);

        assertThat(sut.findConflictingApplications(blackoutPeriod)).isEmpty();

        verifyNoInteractions(applicationService);
    }

    @Test
    void findConflictingApplications_keepsOnlyApplicationsOfRestrictedVacationTypes() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final VacationType<?> restrictedVacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Application restrictedApplication = new Application();
        restrictedApplication.setPerson(person);
        restrictedApplication.setStartDate(LocalDate.of(2026, 12, 22));
        restrictedApplication.setEndDate(LocalDate.of(2026, 12, 23));
        restrictedApplication.setVacationType(restrictedVacationType);

        final Application otherApplication = new Application();
        otherApplication.setPerson(person);
        otherApplication.setStartDate(LocalDate.of(2026, 12, 28));
        otherApplication.setEndDate(LocalDate.of(2026, 12, 29));
        otherApplication.setVacationType(createVacationType(2L, SPECIALLEAVE, new StaticMessageSource()));

        when(applicationService.getApplicationsForACertainPeriodAndStatus(
            eq(LocalDate.of(2026, 12, 20)), eq(LocalDate.of(2027, 1, 5)), eq(List.of(person)), eq(activeStatuses())))
            .thenReturn(List.of(otherApplication, restrictedApplication));

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));
        blackoutPeriod.setCompanyWide(true);
        blackoutPeriod.setVacationTypes(List.of(restrictedVacationType));

        assertThat(sut.findConflictingApplications(blackoutPeriod)).containsExactly(restrictedApplication);
    }

    @Test
    void findBlockingBlackoutPeriod_returnsEmptyWhenDepartmentScopedBlackoutHasNoDepartmentsLeft() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(departmentService.getDepartmentIdsByMembers(List.of(person))).thenReturn(Map.of(new PersonId(1L), Set.of(42L)));

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Vertriebssperre");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setCompanyWide(false);
        entity.setAllVacationTypes(true);
        // all departments of the blackout period have been deleted
        entity.setDepartmentIds(Set.of());
        when(blackoutPeriodRepository.findOverlapping(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23))).thenReturn(List.of(entity));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Optional<BlackoutPeriod> blockingBlackoutPeriod = sut.findBlockingBlackoutPeriod(
            person, LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 23), vacationType);

        assertThat(blockingBlackoutPeriod).isEmpty();
    }

    @Test
    void create_persistsScopeFlags() {

        when(blackoutPeriodRepository.save(any(BlackoutPeriodEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setTitle("Jahresabschluss");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));
        blackoutPeriod.setCompanyWide(true);
        blackoutPeriod.setAllVacationTypes(true);

        final BlackoutPeriod created = sut.create(blackoutPeriod);

        final ArgumentCaptor<BlackoutPeriodEntity> captor = ArgumentCaptor.forClass(BlackoutPeriodEntity.class);
        verify(blackoutPeriodRepository).save(captor.capture());
        assertThat(captor.getValue().isCompanyWide()).isTrue();
        assertThat(captor.getValue().isAllVacationTypes()).isTrue();
        assertThat(created.isCompanyWide()).isTrue();
        assertThat(created.appliesToAllVacationTypes()).isTrue();
    }

    @Test
    void removeDeletedDepartment_removesTheDepartmentFromAffectedBlackoutPeriodsAndKeepsThem() {

        final BlackoutPeriodEntity affected = new BlackoutPeriodEntity();
        affected.setId(1L);
        affected.setDepartmentIds(Set.of(42L, 43L));

        final BlackoutPeriodEntity onlyDeletedDepartment = new BlackoutPeriodEntity();
        onlyDeletedDepartment.setId(2L);
        onlyDeletedDepartment.setDepartmentIds(Set.of(42L));

        final BlackoutPeriodEntity unaffected = new BlackoutPeriodEntity();
        unaffected.setId(3L);
        unaffected.setDepartmentIds(Set.of(43L));

        when(blackoutPeriodRepository.findAll()).thenReturn(List.of(affected, onlyDeletedDepartment, unaffected));

        sut.removeDeletedDepartment(DepartmentDeletedEvent.of(42L));

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<BlackoutPeriodEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(blackoutPeriodRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).containsExactly(affected, onlyDeletedDepartment);
        assertThat(affected.getDepartmentIds()).containsExactly(43L);
        assertThat(onlyDeletedDepartment.getDepartmentIds()).isEmpty();
        assertThat(unaffected.getDepartmentIds()).containsExactly(43L);
        verify(blackoutPeriodRepository, never()).delete(any());
    }
}
