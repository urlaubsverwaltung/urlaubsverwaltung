package org.synyx.urlaubsverwaltung.absence;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCEL_RE;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;


@Service
public class AbsenceServiceImpl implements AbsenceService {

    private static final List<ApplicationStatus> APPLICATION_STATUSES = List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCEL_RE);
    private static final List<SickNoteStatus> SICK_NOTE_STATUSES = List.of(ACTIVE);

    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final SettingsService settingsService;

    @Autowired
    public AbsenceServiceImpl(ApplicationService applicationService, SickNoteService sickNoteService, SettingsService settingsService) {

        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.settingsService = settingsService;
    }

    @Override
    public List<Absence> getOpenAbsences(List<Person> persons) {
        final List<Application> openApplications = applicationService.getForStatesAndPerson(APPLICATION_STATUSES, persons);
        final List<Absence> applicationAbsences = generateAbsencesFromApplication(openApplications);

        final List<SickNote> openSickNotes = sickNoteService.getForStatesAndPerson(SICK_NOTE_STATUSES, persons);
        final List<Absence> sickNoteAbsences = generateAbsencesFromSickNotes(openSickNotes);

        return ListUtils.union(applicationAbsences, sickNoteAbsences);
    }

    @Override
    public List<Absence> getOpenAbsences() {
        final List<Application> openApplications = applicationService.getForStates(APPLICATION_STATUSES);
        final List<Absence> applicationAbsences = generateAbsencesFromApplication(openApplications);

        final List<SickNote> openSickNotes = sickNoteService.getForStates(SICK_NOTE_STATUSES);
        final List<Absence> sickNoteAbsences = generateAbsencesFromSickNotes(openSickNotes);

        return ListUtils.union(applicationAbsences, sickNoteAbsences);
    }

    private List<Absence> generateAbsencesFromApplication(List<Application> applications) {
        final AbsenceTimeConfiguration config = getAbsenceTimeConfiguration();
        return applications.stream()
            .map(application -> new Absence(application.getPerson(), application.getPeriod(), config))
            .collect(toList());
    }

    private List<Absence> generateAbsencesFromSickNotes(List<SickNote> sickNotes) {
        final AbsenceTimeConfiguration config = getAbsenceTimeConfiguration();
        return sickNotes.stream()
            .map(sickNote -> new Absence(sickNote.getPerson(), sickNote.getPeriod(), config))
            .collect(toList());
    }

    private AbsenceTimeConfiguration getAbsenceTimeConfiguration() {
        final CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        return new AbsenceTimeConfiguration(calendarSettings);
    }
}
