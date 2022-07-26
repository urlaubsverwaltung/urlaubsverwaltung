package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SickNoteDetailedStatisticsServiceTest {

    private SickNoteDetailedStatisticsService sickNoteDetailedStatisticsService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private PersonBasedataService personBasedataService;
    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sickNoteDetailedStatisticsService = new SickNoteDetailedStatisticsService(sickNoteService, personBasedataService, departmentService);
    }

    @Test
    void getAllSicknotes() {

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        Person person = new Person();
        person.setFirstName("Lazy");
        person.setLastName("Bone");
        person.setId(42);
        String personnnelNumber = "Passagier1337";
        PersonBasedata personBasedata = new PersonBasedata(person.getId(), personnnelNumber, "additionalInfo");

        final SickNote sickNote = new SickNote();
        sickNote.setPerson(person);
        sickNote.setStartDate(startDate.plusDays(5));
        sickNote.setEndDate(startDate.plusDays(6));
        List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getByPeriod(filterPeriod.getStartDate(), filterPeriod.getEndDate())).thenReturn(sickNotes);
        Map<Integer, PersonBasedata> personIdBasedatamap = Map.of(person.getId(), personBasedata);
        when(personBasedataService.getBasedataByPersonIds(List.of(person.getId()))).thenReturn(personIdBasedatamap);
        List<String> departmentNames = List.of("Kitchen", "Service");
        Map<Integer, List<String>> personIdDepartmentMap = Map.of(person.getId(), departmentNames);
        when(departmentService.getDepartmentsByMembers(List.of(person))).thenReturn(personIdDepartmentMap);

        final List<SickNoteDetailedStatistics> allSicknotes = sickNoteDetailedStatisticsService.getAllSicknotes(filterPeriod);

        assertThat(allSicknotes)
            .extracting(SickNoteDetailedStatistics::getPersonalNumber,
                SickNoteDetailedStatistics::getFirstName,
                SickNoteDetailedStatistics::getLastName,
                SickNoteDetailedStatistics::getDepartments,
                SickNoteDetailedStatistics::getSickNotes
            )
            .contains(tuple(personnnelNumber, "Lazy", "Bone", departmentNames, sickNotes));
    }
}
