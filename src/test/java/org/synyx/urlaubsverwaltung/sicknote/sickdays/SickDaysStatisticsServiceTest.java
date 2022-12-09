package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
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

    @BeforeEach
    void setUp() {
        sut = new SickDaysStatisticsService(sickNoteService, departmentService, personBasedataService);
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsDepartmentHeadAndSickNoteView() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        departmentHead.setFirstName("Department");
        departmentHead.setLastName("Head");
        departmentHead.setId(42);

        final String personnnelNumber = "Passagier1337";
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(departmentHead.getId()), personnnelNumber, "additionalInfo");

        final SickNote sickNote = SickNote.builder()
                .person(departmentHead)
                .startDate(startDate.plusDays(5))
                .endDate(startDate.plusDays(6))
                .build();

        final Person member = new Person();
        when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(List.of(member));
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(member), startDate, endDate)).thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(departmentHead.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(departmentHead.getId()))).thenReturn(personIdBasedatamap);

        when(departmentService.getDepartmentNamesByMembers(List.of(departmentHead))).thenReturn(Map.of(new PersonId(departmentHead.getId()), List.of("Kitchen", "Service")));

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(departmentHead, startDate, endDate);
        assertThat(allSicknotes).hasSize(1);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("Passagier1337");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Department");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Head");
            assertThat(actual.getDepartments()).containsExactly("Kitchen", "Service");
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
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
        departmentHead.setId(42);

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(departmentHead, startDate, endDate);
        assertThat(allSicknotes).isEmpty();
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsSecondStageAuthorityAndSickNoteView() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW));
        secondStageAuthority.setFirstName("Second");
        secondStageAuthority.setLastName("Stage");
        secondStageAuthority.setId(42);

        final String personnelNumber = "Passagier1337";
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(secondStageAuthority.getId()), personnelNumber, "additionalInfo");

        final SickNote sickNote = SickNote.builder()
                .person(secondStageAuthority)
                .startDate(startDate.plusDays(5))
                .endDate(startDate.plusDays(6))
                .build();

        final Person member = new Person();
        when(departmentService.getMembersForSecondStageAuthority(secondStageAuthority)).thenReturn(List.of(member));
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(member), startDate, endDate)).thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(secondStageAuthority.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(secondStageAuthority.getId()))).thenReturn(personIdBasedatamap);

        when(departmentService.getDepartmentNamesByMembers(List.of(secondStageAuthority))).thenReturn(Map.of(new PersonId(secondStageAuthority.getId()), List.of("Kitchen", "Service")));

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(secondStageAuthority, startDate, endDate);
        assertThat(allSicknotes).hasSize(1);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
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
        secondStageAuthority.setId(42);

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(secondStageAuthority, startDate, endDate);
        assertThat(allSicknotes).isEmpty();
    }

    @Test
    void ensureCreatesSickNoteDetailedStatisticsAsOffice() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        office.setFirstName("Office");
        office.setLastName("Person");
        office.setId(42);

        final String personnelNumber = "Passagier1337";
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(office.getId()), personnelNumber, "additionalInfo");

        final SickNote sickNote = SickNote.builder()
                .person(office)
                .startDate(startDate.plusDays(5))
                .endDate(startDate.plusDays(6))
                .build();

        when(sickNoteService.getAllActiveByPeriod(startDate, endDate)).thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(office.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(office.getId()))).thenReturn(personIdBasedatamap);

        when(departmentService.getDepartmentNamesByMembers(List.of(office))).thenReturn(Map.of(new PersonId(office.getId()), List.of("Kitchen", "Service")));

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(office, startDate, endDate);
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
        office.setId(42);

        final String personnelNumber = "Passagier1337";
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(office.getId()), personnelNumber, "additionalInfo");

        final SickNote sickNote = SickNote.builder()
                .person(office)
                .startDate(startDate.plusDays(5))
                .endDate(startDate.plusDays(6))
                .build();

        when(sickNoteService.getAllActiveByPeriod(startDate, endDate)).thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(office.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(office.getId()))).thenReturn(personIdBasedatamap);

        when(departmentService.getDepartmentNamesByMembers(List.of(office))).thenReturn(Map.of(new PersonId(office.getId()), List.of("Kitchen", "Service")));

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(office, startDate, endDate);
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
        boss.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW));
        boss.setFirstName("Boss");
        boss.setLastName("Person");
        boss.setId(42);

        final String personnelNumber = "Passagier1337";
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(boss.getId()), personnelNumber, "additionalInfo");

        final SickNote sickNote = SickNote.builder()
                .person(boss)
                .startDate(startDate.plusDays(5))
                .endDate(startDate.plusDays(6))
                .build();

        when(sickNoteService.getAllActiveByPeriod(startDate, endDate)).thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(boss.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(boss.getId()))).thenReturn(personIdBasedatamap);

        when(departmentService.getDepartmentNamesByMembers(List.of(boss))).thenReturn(Map.of(new PersonId(boss.getId()), List.of("Kitchen", "Service")));

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(boss, startDate, endDate);
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
        boss.setId(42);

        final String personnelNumber = "Passagier1337";
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(boss.getId()), personnelNumber, "additionalInfo");

        final SickNote sickNote = SickNote.builder()
                .person(boss)
                .startDate(startDate.plusDays(5))
                .endDate(startDate.plusDays(6))
                .build();

        when(sickNoteService.getAllActiveByPeriod(startDate, endDate)).thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(boss.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(boss.getId()))).thenReturn(personIdBasedatamap);

        when(departmentService.getDepartmentNamesByMembers(List.of(boss))).thenReturn(Map.of(new PersonId(boss.getId()), List.of("Kitchen", "Service")));

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(boss, startDate, endDate);
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
        boss.setId(42);

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(boss, startDate, endDate);
        assertThat(allSicknotes).isEmpty();
    }

    @Test
    void ensureStatisticsCanBeCreatedWithPersonWithoutDepartments() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        departmentHead.setFirstName("Department");
        departmentHead.setLastName("Head");
        departmentHead.setId(42);

        final String personnelNumber = "Passagier1337";
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(departmentHead.getId()), personnelNumber, "additionalInfo");

        final SickNote sickNote = SickNote.builder()
                .person(departmentHead)
                .startDate(startDate.plusDays(5))
                .endDate(startDate.plusDays(6))
                .build();

        final Person member = new Person();
        when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(List.of(member));
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(member), startDate, endDate)).thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBasedatamap = Map.of(new PersonId(departmentHead.getId()), personBasedata);
        when(personBasedataService.getBasedataByPersonId(List.of(departmentHead.getId()))).thenReturn(personIdBasedatamap);

        when(departmentService.getDepartmentNamesByMembers(List.of(departmentHead))).thenReturn(Map.of());

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(departmentHead, startDate, endDate);
        assertThat(allSicknotes).hasSize(1);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("Passagier1337");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Department");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Head");
            assertThat(actual.getDepartments()).isEmpty();
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
    }

    @Test
    void ensureStatisticsCanBeCreatedWithPersonWithoutBaseData() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        departmentHead.setFirstName("Department");
        departmentHead.setLastName("Head");
        departmentHead.setId(42);

        final SickNote sickNote = SickNote.builder()
                .person(departmentHead)
                .startDate(startDate.plusDays(5))
                .endDate(startDate.plusDays(6))
                .build();

        final Person member = new Person();
        when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(List.of(member));
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(member), startDate, endDate)).thenReturn(List.of(sickNote));

        final Map<PersonId, PersonBasedata> personIdBaseDataMap = Map.of();
        when(personBasedataService.getBasedataByPersonId(List.of(departmentHead.getId()))).thenReturn(personIdBaseDataMap);

        when(departmentService.getDepartmentNamesByMembers(List.of(departmentHead))).thenReturn(Map.of(new PersonId(departmentHead.getId()), List.of("Kitchen", "Service")));

        final List<SickDaysDetailedStatistics> allSicknotes = sut.getAll(departmentHead, startDate, endDate);
        assertThat(allSicknotes).hasSize(1);
        assertThat(allSicknotes.get(0)).satisfies(actual -> {
            assertThat(actual.getPersonalNumber()).isEqualTo("");
            assertThat(actual.getPerson().getFirstName()).isEqualTo("Department");
            assertThat(actual.getPerson().getLastName()).isEqualTo("Head");
            assertThat(actual.getDepartments()).containsExactly("Kitchen", "Service");
            assertThat(actual.getSickNotes()).containsExactly(sickNote);
        });
    }
}
