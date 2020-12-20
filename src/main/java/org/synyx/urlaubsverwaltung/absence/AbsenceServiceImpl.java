package org.synyx.urlaubsverwaltung.absence;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;


@Service
public class AbsenceServiceImpl implements AbsenceService {

    private static final List<ApplicationStatus> APPLICATION_STATUSES = List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
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
    public List<Absence> getOpenAbsencesSince(List<Person> persons, LocalDate since) {
        final List<Application> openApplications = applicationService.getForStatesAndPersonSince(APPLICATION_STATUSES, persons, since);
        final List<Absence> applicationAbsences = generateAbsencesFromApplication(openApplications);

        final List<SickNote> openSickNotes = sickNoteService.getForStatesAndPersonSince(SICK_NOTE_STATUSES, persons, since);
        final List<Absence> sickNoteAbsences = generateAbsencesFromSickNotes(openSickNotes);

        return ListUtils.union(applicationAbsences, sickNoteAbsences);
    }

    @Override
    public List<Absence> getOpenAbsencesSince(LocalDate since) {
        final List<Application> openApplications = applicationService.getForStatesSince(APPLICATION_STATUSES, since);
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
        final TimeSettings timeSettings = settingsService.getSettings().getTimeSettings();
        return new AbsenceTimeConfiguration(timeSettings);
    }
}
