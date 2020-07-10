package org.synyx.urlaubsverwaltung.absence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createSickNote;

@RunWith(MockitoJUnitRunner.class)
public class AbsenceServiceImplTest {

    private AbsenceServiceImpl sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private SickNoteService sickNoteService;

    @Before
    public void setUp() {
        sut = new AbsenceServiceImpl(applicationService, sickNoteService, settingsService);
    }

    @Test
    public void getOpenAbsencesForPersons() {

        final Settings settings = new Settings();
        settings.setCalendarSettings(new CalendarSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = createPerson();

        final LocalDate startDate = LocalDate.of(2019, 12, 10);
        final LocalDate endDate = LocalDate.of(2019, 12, 23);
        final Application application = createApplication(person, startDate, endDate, FULL);
        when(applicationService.getForStatesAndPerson(eq(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED)), eq(List.of(person)))).thenReturn(List.of(application));

        final LocalDate startDateSickNote = LocalDate.of(2019, 10, 10);
        final LocalDate endDateSickNote = LocalDate.of(2019, 10, 23);
        final SickNote sickNote = createSickNote(person, startDateSickNote, endDateSickNote, FULL);
        when(sickNoteService.getForStatesAndPerson(eq(List.of(ACTIVE)), eq(List.of(person)))).thenReturn(List.of(sickNote));

        final List<Absence> openAbsences = sut.getOpenAbsences(List.of(person));
        assertThat(openAbsences).hasSize(2);
        assertThat(openAbsences.get(0).getPerson()).isEqualTo(person);
        assertThat(openAbsences.get(0).getStartDate()).isEqualTo("2019-12-10T00:00Z");
        assertThat(openAbsences.get(0).getEndDate()).isEqualTo("2019-12-24T00:00Z");
        assertThat(openAbsences.get(1).getPerson()).isEqualTo(person);
        assertThat(openAbsences.get(1).getStartDate()).isEqualTo("2019-10-10T00:00Z");
        assertThat(openAbsences.get(1).getEndDate()).isEqualTo("2019-10-24T00:00Z");
    }

    @Test
    public void getOpenAbsences() {

        final Settings settings = new Settings();
        settings.setCalendarSettings(new CalendarSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = createPerson();

        final LocalDate startDate = LocalDate.of(2019, 11, 10);
        final LocalDate endDate = LocalDate.of(2019, 11, 23);
        final Application application = createApplication(person, startDate, endDate, FULL);
        when(applicationService.getForStates(eq(List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED)))).thenReturn(List.of(application));

        final LocalDate startDateSickNote = LocalDate.of(2019, 10, 10);
        final LocalDate endDateSickNote = LocalDate.of(2019, 10, 23);
        final SickNote sickNote = createSickNote(person, startDateSickNote, endDateSickNote, FULL);
        when(sickNoteService.getForStates(eq(List.of(ACTIVE)))).thenReturn(List.of(sickNote));

        final List<Absence> openAbsences = sut.getOpenAbsences();
        assertThat(openAbsences).hasSize(2);
        assertThat(openAbsences.get(0).getPerson()).isEqualTo(person);
        assertThat(openAbsences.get(0).getStartDate()).isEqualTo("2019-11-10T00:00Z");
        assertThat(openAbsences.get(0).getEndDate()).isEqualTo("2019-11-24T00:00Z");
        assertThat(openAbsences.get(1).getPerson()).isEqualTo(person);
        assertThat(openAbsences.get(1).getStartDate()).isEqualTo("2019-10-10T00:00Z");
        assertThat(openAbsences.get(1).getEndDate()).isEqualTo("2019-10-24T00:00Z");
    }
}
