package org.synyx.urlaubsverwaltung.absence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;


@Service
public class AbsenceServiceImpl implements AbsenceService {

    private final ApplicationService applicationService;
    private final SettingsService settingsService;

    @Autowired
    public AbsenceServiceImpl(ApplicationService applicationService, SettingsService settingsService) {

        this.applicationService = applicationService;
        this.settingsService = settingsService;
    }

    @Override
    public List<Absence> getOpenAbsences(List<Person> persons) {
        final List<ApplicationStatus> openStatuses = List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED);
        final List<Application> openApplications = applicationService.getForStatesAndPerson(openStatuses, persons);
        return generateAbsences(openApplications);
    }

    @Override
    public List<Absence> getOpenAbsences(){
        final List<ApplicationStatus> openStatuses = List.of(ALLOWED, WAITING, TEMPORARY_ALLOWED);
        final List<Application> openApplications = applicationService.getForStates(openStatuses);
        return generateAbsences(openApplications);
    }

    private List<Absence> generateAbsences(List<Application> applications) {

        final CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        final AbsenceTimeConfiguration config = new AbsenceTimeConfiguration(calendarSettings);

        return applications.stream()
            .map(application -> new Absence(application.getPerson(), application.getPeriod(), config))
            .collect(toList());
    }

}
