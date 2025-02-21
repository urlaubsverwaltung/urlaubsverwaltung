package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@ExtendWith(MockitoExtension.class)
class SickDaysStatisticsServiceTest {

    private SickDaysStatisticsService sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonBasedataService personBasedataService;
    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new SickDaysStatisticsService(sickNoteService, departmentService, personBasedataService, personService);
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsDepartmentHeadAndSickNoteView() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        departmentHead.setFirstName("Department");
        departmentHead.setLastName("Head");
        departmentHead.setId(42L);

        final Person member = new Person();
        member.setId(2L);
        member.setFirstName("John");
        member.setLastName("Doe");

        final SickNote sickNote = SickNote.builder()
            .person(departmentHead)
            .startDate(startDate.plusDays(5))
            .endDate(startDate.plusDays(6))
            .build();

        when(departmentService.getManagedMembersOfPerson(departmentHead, new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(departmentHead, member)));

        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(departmentHead, member), startDate, endDate)).thenReturn(List.of(sickNote));

        final PersonBasedata departmentHeadBasedata = new PersonBasedata(new PersonId(departmentHead.getId()), "Passagier1337", "additionalInfo");
        when(personBasedataService.getBasedataByPersonId(List.of(departmentHead.getId(), member.getId()))).thenReturn(Map.of(new PersonId(departmentHead.getId()), departmentHeadBasedata));

        when(departmentService.getDepartmentNamesByMembers(List.of(departmentHead, member)))
            .thenReturn(Map.of(new PersonId(departmentHead.getId()), List.of("Kitchen", "Service"), new PersonId(member.getId()), List.of("Service")));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(departmentHead, startDate, endDate, pageableSearchQuery);

        final List<SickDaysDetailedStatistics> allSicknotes = allSicknotesPage.getContent();
        assertThat(allSicknotes).hasSize(2);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("Passagier1337");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Department");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Head");
            assertThat(actual.getDepartments()).containsExactly("Kitchen", "Service");
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
        assertThat(allSicknotes.get(1)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEmpty();
            assertThat(actual.getPerson().getFirstName()).isEqualTo("John");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Doe");
            assertThat(actual.getDepartments()).containsExactly("Service");
            assertThat(actual.getSickNotes()).isEmpty();
        });
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsDepartmentHeadWithoutSickNoteView() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        departmentHead.setFirstName("Department");
        departmentHead.setLastName("Head");
        departmentHead.setId(42L);

        when(departmentService.getManagedMembersOfPerson(departmentHead, new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(departmentHead)));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(departmentHead, startDate, endDate, pageableSearchQuery);

        assertThat(allSicknotesPage.getContent()).hasSize(1);
        assertThat(allSicknotesPage.getContent().get(0)).satisfies(statistics -> assertThat(statistics.getSickNotes()).isEmpty());
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsSecondStageAuthorityAndSickNoteView() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setId(42L);
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW));
        secondStageAuthority.setFirstName("Second");
        secondStageAuthority.setLastName("Stage");

        final Person member = new Person();
        member.setId(2L);
        member.setFirstName("John");
        member.setLastName("Doe");

        final SickNote sickNote = SickNote.builder()
            .person(secondStageAuthority)
            .startDate(startDate.plusDays(5))
            .endDate(startDate.plusDays(6))
            .build();

        when(departmentService.getManagedMembersOfPerson(secondStageAuthority, new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(member, secondStageAuthority)));

        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(member, secondStageAuthority), startDate, endDate)).thenReturn(List.of(sickNote));

        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(secondStageAuthority.getId()), "Passagier1337", "additionalInfo");
        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(secondStageAuthority.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(member.getId(), secondStageAuthority.getId()))).thenReturn(personIdBasedatamap);

        when(departmentService.getDepartmentNamesByMembers(List.of(member, secondStageAuthority)))
            .thenReturn(Map.of(new PersonId(secondStageAuthority.getId()), List.of("Kitchen", "Service"), new PersonId(member.getId()), List.of("Service")));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(secondStageAuthority, startDate, endDate, pageableSearchQuery);

        final List<SickDaysDetailedStatistics> allSicknotes = allSicknotesPage.getContent();
        assertThat(allSicknotes).hasSize(2);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEmpty();
            assertThat(actual.getPerson().getFirstName()).isEqualTo("John");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Doe");
            assertThat(actual.getDepartments()).containsExactly("Service");
            assertThat(actual.getSickNotes()).isEmpty();
        });
        assertThat(allSicknotes.get(1)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("Passagier1337");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Second");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Stage");
            assertThat(actual.getDepartments()).containsExactly("Kitchen", "Service");
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsSecondStageAuthorityWithoutSickNoteView() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        secondStageAuthority.setFirstName("Second");
        secondStageAuthority.setLastName("Stage");
        secondStageAuthority.setId(42L);

        when(departmentService.getManagedMembersOfPerson(secondStageAuthority, new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(secondStageAuthority)));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(secondStageAuthority, startDate, endDate, pageableSearchQuery);

        assertThat(allSicknotesPage.getContent()).hasSize(1);
        assertThat(allSicknotesPage.getContent().get(0)).satisfies(statistics -> assertThat(statistics.getSickNotes()).isEmpty());
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsOffice() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person office = new Person();
        office.setId(42L);
        office.setPermissions(List.of(USER, OFFICE));
        office.setFirstName("Office");
        office.setLastName("Person");

        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(office.getId()), "Passagier1337", "additionalInfo");

        final SickNote sickNote = SickNote.builder()
            .person(office)
            .startDate(startDate.plusDays(5))
            .endDate(startDate.plusDays(6))
            .build();

        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(office), startDate, endDate))
            .thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(office.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(office.getId()))).thenReturn(personIdBasedatamap);

        when(personService.getActivePersons(new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(office)));

        when(departmentService.getDepartmentNamesByMembers(List.of(office))).thenReturn(Map.of(new PersonId(office.getId()), List.of("Kitchen", "Service")));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(office, startDate, endDate, pageableSearchQuery);

        final List<SickDaysDetailedStatistics> allSicknotes = allSicknotesPage.getContent();
        assertThat(allSicknotes).hasSize(1);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("Passagier1337");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Office");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Person");
            assertThat(actual.getDepartments()).containsExactly("Kitchen", "Service");
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureCreatesSickNoteDetailedStatisticsAsOfficeWithDepartmentRole(Role departmentRole) {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE, departmentRole, SICK_NOTE_VIEW));
        office.setFirstName("Office");
        office.setLastName("Person");
        office.setId(42L);

        final String personnelNumber = "Passagier1337";
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(office.getId()), personnelNumber, "additionalInfo");

        final SickNote sickNote = SickNote.builder()
            .person(office)
            .startDate(startDate.plusDays(5))
            .endDate(startDate.plusDays(6))
            .build();

        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(office), startDate, endDate))
            .thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(office.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(office.getId()))).thenReturn(personIdBasedatamap);

        when(personService.getActivePersons(new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(office)));

        when(departmentService.getDepartmentNamesByMembers(List.of(office))).thenReturn(Map.of(new PersonId(office.getId()), List.of("Kitchen", "Service")));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(office, startDate, endDate, pageableSearchQuery);

        final List<SickDaysDetailedStatistics> allSicknotes = allSicknotesPage.getContent();
        assertThat(allSicknotes).hasSize(1);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("Passagier1337");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Office");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Person");
            assertThat(actual.getDepartments()).containsExactly("Kitchen", "Service");
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsBossAndSickNoteView() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person boss = new Person();
        boss.setId(42L);
        boss.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW));
        boss.setFirstName("Boss");
        boss.setLastName("Person");

        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(boss.getId()), "Passagier1337", "additionalInfo");

        final SickNote sickNote = SickNote.builder()
            .person(boss)
            .startDate(startDate.plusDays(5))
            .endDate(startDate.plusDays(6))
            .build();

        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(boss), startDate, endDate))
            .thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(boss.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(boss.getId()))).thenReturn(personIdBasedatamap);

        when(personService.getActivePersons(new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(boss)));

        when(departmentService.getDepartmentNamesByMembers(List.of(boss)))
            .thenReturn(Map.of(new PersonId(boss.getId()), List.of("Kitchen", "Service")));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(boss, startDate, endDate, pageableSearchQuery);

        final List<SickDaysDetailedStatistics> allSicknotes = allSicknotesPage.getContent();
        assertThat(allSicknotes).hasSize(1);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("Passagier1337");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Boss");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Person");
            assertThat(actual.getDepartments()).containsExactly("Kitchen", "Service");
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureCreatesSickNoteDetailedStatisticsAsBossAndDepartmentRoleAndSickNoteView(Role departmentRole) {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person boss = new Person();
        boss.setPermissions(List.of(USER, BOSS, departmentRole, SICK_NOTE_VIEW));
        boss.setFirstName("Boss");
        boss.setLastName("Person");
        boss.setId(42L);

        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(boss.getId()), "Passagier1337", "additionalInfo");

        final SickNote sickNote = SickNote.builder()
            .person(boss)
            .startDate(startDate.plusDays(5))
            .endDate(startDate.plusDays(6))
            .build();

        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(boss), startDate, endDate))
            .thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(boss.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(boss.getId()))).thenReturn(personIdBasedatamap);

        when(personService.getActivePersons(new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(boss)));

        when(departmentService.getDepartmentNamesByMembers(List.of(boss)))
            .thenReturn(Map.of(new PersonId(boss.getId()), List.of("Kitchen", "Service")));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(boss, startDate, endDate, pageableSearchQuery);

        final List<SickDaysDetailedStatistics> allSicknotes = allSicknotesPage.getContent();
        assertThat(allSicknotes).hasSize(1);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("Passagier1337");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Boss");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Person");
            assertThat(actual.getDepartments()).containsExactly("Kitchen", "Service");
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsBossWithoutSickNoteView() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person boss = new Person();
        boss.setPermissions(List.of(USER, BOSS));
        boss.setFirstName("Boss");
        boss.setLastName("Person");
        boss.setId(42L);

        when(departmentService.getManagedMembersOfPerson(boss, new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(boss)));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(boss, startDate, endDate, pageableSearchQuery);

        assertThat(allSicknotesPage.getContent()).hasSize(1);
        assertThat(allSicknotesPage.getContent().get(0)).satisfies(statistics -> assertThat(statistics.getSickNotes()).isEmpty());
    }

    @Test
    void ensureStatisticsCanBeCreatedWithPersonWithoutDepartments() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person departmentHead = new Person();
        departmentHead.setId(42L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        departmentHead.setFirstName("Department");
        departmentHead.setLastName("Head");

        final Person member = new Person();
        member.setId(2L);
        member.setFirstName("John");
        member.setLastName("Doe");

        final SickNote sickNote = SickNote.builder()
            .person(departmentHead)
            .startDate(startDate.plusDays(5))
            .endDate(startDate.plusDays(6))
            .build();

        when(departmentService.getManagedMembersOfPerson(departmentHead, new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(departmentHead, member)));

        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(departmentHead, member), startDate, endDate)).thenReturn(List.of(sickNote));

        final PersonBasedata departmentHeadBasedata = new PersonBasedata(new PersonId(departmentHead.getId()), "Passagier1337", "additionalInfo");
        final PersonBasedata memberBasedata = new PersonBasedata(new PersonId(2L), "000042", "additionalInfo member");
        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(departmentHead.getId()), departmentHeadBasedata, new PersonId(2L), memberBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(42L, 2L))).thenReturn(personIdBasedatamap);

        when(departmentService.getDepartmentNamesByMembers(List.of(departmentHead, member))).thenReturn(Map.of());

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(departmentHead, startDate, endDate, pageableSearchQuery);

        final List<SickDaysDetailedStatistics> allSicknotes = allSicknotesPage.getContent();
        assertThat(allSicknotes).hasSize(2);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("Passagier1337");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Department");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Head");
            assertThat(actual.getDepartments()).isEmpty();
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
        assertThat(allSicknotes.get(1)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("000042");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("John");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Doe");
            assertThat(actual.getDepartments()).isEmpty();
            assertThat(actual.getSickNotes()).isEmpty();
        });
    }

    @Test
    void ensureStatisticsCanBeCreatedWithPersonWithoutBaseData() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person departmentHead = new Person();
        departmentHead.setId(42L);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        departmentHead.setFirstName("Department");
        departmentHead.setLastName("Head");

        final Person member = new Person();
        member.setId(2L);
        member.setFirstName("John");
        member.setLastName("Doe");

        final SickNote sickNote = SickNote.builder()
            .person(departmentHead)
            .startDate(startDate.plusDays(5))
            .endDate(startDate.plusDays(6))
            .build();

        when(departmentService.getManagedMembersOfPerson(departmentHead, new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("firstName")), "")))
            .thenReturn(new PageImpl<>(List.of(departmentHead, member)));

        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(departmentHead, member), startDate, endDate)).thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBaseDataMap = Map.of();
        when(personBasedataService.getBasedataByPersonId(List.of(42L, 2L))).thenReturn(personIdBaseDataMap);

        when(departmentService.getDepartmentNamesByMembers(List.of(departmentHead, member)))
            .thenReturn(Map.of(new PersonId(42L), List.of("Kitchen", "Service"), new PersonId(2L), List.of("Service")));

        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 10, Sort.by("person.firstName")), "");
        final Page<SickDaysDetailedStatistics> allSicknotesPage = sut.getAll(departmentHead, startDate, endDate, pageableSearchQuery);

        final List<SickDaysDetailedStatistics> allSicknotes = allSicknotesPage.getContent();
        assertThat(allSicknotes).hasSize(2);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEmpty();
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Department");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Head");
            assertThat(actual.getDepartments()).containsExactly("Kitchen", "Service");
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
        assertThat(allSicknotes.get(1)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEmpty();
            assertThat(actual.getPerson().getFirstName()).isEqualTo("John");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Doe");
            assertThat(actual.getDepartments()).containsExactly("Service");
            assertThat(actual.getSickNotes()).isEmpty();
        });
    }
}
