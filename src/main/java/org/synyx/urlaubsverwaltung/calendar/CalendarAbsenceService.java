package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;

import java.time.LocalDate;
import java.util.List;

@Service
class CalendarAbsenceService {

    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final SettingsService settingsService;

    @Autowired
    CalendarAbsenceService(
            ApplicationService applicationService,
            SickNoteService sickNoteService,
            SettingsService settingsService
    ) {
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.settingsService = settingsService;
    }

    List<CalendarAbsence> getOpenAbsencesSince(List<Person> persons, LocalDate since) {
        final List<Application> openApplications = applicationService.getForStatesAndPersonSince(ApplicationStatus.activeStatuses(), persons, since);
        final List<CalendarAbsence> applicationAbsences = generateAbsencesFromApplication(openApplications);

        final List<SickNote> openSickNotes = sickNoteService.getForStatesAndPersonSince(SickNoteStatus.activeStatuses(), persons, since);
        final List<CalendarAbsence> sickNoteAbsences = generateAbsencesFromSickNotes(openSickNotes);

        return ListUtils.union(applicationAbsences, sickNoteAbsences);
    }

    List<CalendarAbsence> getOpenAbsencesSince(LocalDate since) {
        final List<Application> openApplications = applicationService.getForStatesSince(ApplicationStatus.activeStatuses(), since);
        final List<CalendarAbsence> applicationAbsences = generateAbsencesFromApplication(openApplications);

        final List<SickNote> openSickNotes = sickNoteService.getForStatesSince(SickNoteStatus.activeStatuses(), since);
        final List<CalendarAbsence> sickNoteAbsences = generateAbsencesFromSickNotes(openSickNotes);

        return ListUtils.union(applicationAbsences, sickNoteAbsences);
    }

    private List<CalendarAbsence> generateAbsencesFromApplication(List<Application> applications) {
        final AbsenceTimeConfiguration config = getAbsenceTimeConfiguration();
        return applications.stream()
                .map(application -> new CalendarAbsence(application.getPerson(), application.getPeriod(), config))
                .toList();
    }

    private List<CalendarAbsence> generateAbsencesFromSickNotes(List<SickNote> sickNotes) {
        final AbsenceTimeConfiguration config = getAbsenceTimeConfiguration();
        return sickNotes.stream()
                .map(sickNote -> new CalendarAbsence(sickNote.getPerson(), sickNote.getPeriod(), config))
                .toList();
    }

    private AbsenceTimeConfiguration getAbsenceTimeConfiguration() {
        final TimeSettings timeSettings = settingsService.getSettings().getTimeSettings();
        return new AbsenceTimeConfiguration(timeSettings);
    }
}
