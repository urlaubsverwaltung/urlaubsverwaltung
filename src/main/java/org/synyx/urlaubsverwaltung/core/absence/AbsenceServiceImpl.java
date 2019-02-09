package org.synyx.urlaubsverwaltung.core.absence;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceTimeConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.core.sync.absence.Absence.of;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;


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
    public List<Absence> getOpenAbsences() {

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        AbsenceTimeConfiguration config = new AbsenceTimeConfiguration(calendarSettings);

        List<Application> applications = applicationService.getForStates(asList(ALLOWED, WAITING, TEMPORARY_ALLOWED));
        List<Absence> absences = new ArrayList<>();
        absences.addAll(applications.stream().map(application -> of(application, config)).collect(toList()));

        return absences;
    }
}
