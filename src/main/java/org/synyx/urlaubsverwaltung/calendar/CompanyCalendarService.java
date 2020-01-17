package org.synyx.urlaubsverwaltung.calendar;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;

import java.util.List;


@Service
class CompanyCalendarService {

    private final AbsenceService absenceService;
    private final CompanyCalendarRepository companyCalendarRepository;
    private final ICalService iCalService;

    @Autowired
    CompanyCalendarService(AbsenceService absenceService, CompanyCalendarRepository companyCalendarRepository, ICalService iCalService) {
        this.absenceService = absenceService;
        this.companyCalendarRepository = companyCalendarRepository;
        this.iCalService = iCalService;
    }


    String getCalendarForAll(String secret) {

        if (Strings.isBlank(secret)) {
            throw new IllegalArgumentException("secret must not be empty.");
        }

        final CompanyCalendar calendar = companyCalendarRepository.findBySecret(secret);
        if (calendar == null) {
            throw new IllegalArgumentException("No calendar found for secret=" + secret);
        }

        final String title = "Abwesenheitskalender der Firma";
        final List<Absence> absences = absenceService.getOpenAbsences();

        return iCalService.generateCalendar(title, absences);
    }
}
