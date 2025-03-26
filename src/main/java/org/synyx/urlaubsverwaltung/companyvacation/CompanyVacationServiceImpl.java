package org.synyx.urlaubsverwaltung.companyvacation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
class CompanyVacationServiceImpl implements CompanyVacationService {

    private final SettingsService settingsService;

    @Autowired
    CompanyVacationServiceImpl(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public Optional<CompanyVacation> getCompanyVacation(LocalDate date) {
        return getCompanyVacations(date, date).stream().findFirst();
    }

    @Override
    public List<CompanyVacation> getCompanyVacations(LocalDate from, LocalDate to) {
        return getCompanyVacations(from, to, getWorkingTimeSettings()).stream().toList();
    }

    private Set<CompanyVacation> getCompanyVacations(final LocalDate from, final LocalDate to, final WorkingTimeSettings workingTimeSettings) {

        final DayLength workingDurationForChristmasEve = workingTimeSettings.getWorkingDurationForChristmasEve();
        final DayLength workingDurationForNewYearsEve = workingTimeSettings.getWorkingDurationForNewYearsEve();

        final DateRange requestRange = new DateRange(from, to);
        final Set<CompanyVacation> holidays = new HashSet<>();

        int year = from.getYear();
        while (year <= to.getYear()) {
            final LocalDate christmas = LocalDate.of(year, Month.DECEMBER, 24);
            if (requestRange.isOverlapping(new DateRange(christmas, christmas))) {
                holidays.add(new CompanyVacation(christmas, workingDurationForChristmasEve.getInverse(), "CHRISTMAS_EVE"));
            }
            final LocalDate newYear = LocalDate.of(year, Month.DECEMBER, 31);
            if (requestRange.isOverlapping(new DateRange(newYear, newYear))) {
                holidays.add(new CompanyVacation(newYear, workingDurationForNewYearsEve.getInverse(), "NEW_YEARS_EVE"));
            }
            year++;
        }

        return holidays;
    }

    private WorkingTimeSettings getWorkingTimeSettings() {
        return settingsService.getSettings().getWorkingTimeSettings();
    }
}
