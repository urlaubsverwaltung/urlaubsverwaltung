package org.synyx.urlaubsverwaltung.absence.statistics;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Comparator.comparing;
import static org.synyx.urlaubsverwaltung.util.CalcUtil.isZero;

/**
 * Controller for the absence statistics page.
 */
@Controller
@RequestMapping("/web/absence/statistics")
class AbsenceStatisticsViewController implements HasLaunchpad, HasPersonSearch {

    private static final int SHARE_SCALE = 3;

    private final AbsenceStatisticsService absenceStatisticsService;
    private final PersonService personService;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final Clock clock;

    AbsenceStatisticsViewController(
        AbsenceStatisticsService absenceStatisticsService,
        PersonService personService,
        PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy,
        PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier,
        Clock clock
    ) {
        this.absenceStatisticsService = absenceStatisticsService;
        this.personService = personService;
        this.defaultPersonSuggestionUrlStrategy = defaultPersonSuggestionUrlStrategy;
        this.personSearchUiFragmentSupplier = personSearchUiFragmentSupplier;
        this.clock = clock;
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return defaultPersonSuggestionUrlStrategy;
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchUiFragmentSupplier;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'BOSS', 'DEPARTMENT_HEAD', 'SECOND_STAGE_AUTHORITY')")
    @GetMapping
    public String absenceStatistics(@RequestParam(value = "year", required = false) Optional<Year> userRequestedYear,
                                     Locale locale, Model model) {

        final Year selectedYear = userRequestedYear.orElse(Year.now(clock));
        final Person signedInUser = personService.getSignedInUser();

        final AbsenceStatistics selectedYearStatistics = absenceStatisticsService.createStatistics(selectedYear, signedInUser);
        model.addAttribute("selectedYearStatistics", selectedYearStatistics);

        model.addAttribute("absenceGraphStatistic", toGraphDto(selectedYearStatistics, locale));
        model.addAttribute("currentYear", Year.now(clock).getValue());

        return "absences/absence_statistics";
    }

    private static GraphDto toGraphDto(AbsenceStatistics statistics, Locale locale) {

        final BigDecimal totalYearSum = statistics.monthlyAbsenceDaysByType().values().stream()
            .map(MonthlyAbsenceDaysByType::yearSum)
            .reduce(ZERO, BigDecimal::add);

        final List<AbsenceTypeDto> types = statistics.monthlyAbsenceDaysByType().entrySet().stream()
            .map(entry -> toAbsenceTypeDto(entry.getKey(), entry.getValue(), totalYearSum, locale))
            .sorted(comparing(AbsenceTypeDto::yearSum).reversed())
            .toList();

        return new GraphDto(types, statistics.vacationDaysTaken().percentage());
    }

    private static AbsenceTypeDto toAbsenceTypeDto(VacationType<?> vacationType, MonthlyAbsenceDaysByType monthlyAbsenceDaysByType,
                                                    BigDecimal totalYearSum, Locale locale) {

        final BigDecimal yearSum = monthlyAbsenceDaysByType.yearSum();
        final BigDecimal share = isZero(totalYearSum)
            ? ZERO
            : yearSum.divide(totalYearSum, SHARE_SCALE, HALF_UP).multiply(valueOf(100));

        return new AbsenceTypeDto(vacationType.getLabel(locale), vacationType.getColor(),
            monthlyAbsenceDaysByType.daysByMonth(), yearSum, share, vacationType.isActive());
    }

    // changing this GraphDto, you may have to increase the local-storage version key in JavaScript to keep the
    // local state clean - mirrors the same warning on SickNoteStatisticsViewController.GraphDto.
    record GraphDto(List<AbsenceTypeDto> types, BigDecimal vacationDaysTakenPercentage) {
    }

    record AbsenceTypeDto(String name, VacationTypeColor color, List<BigDecimal> monthlyDays, BigDecimal yearSum, BigDecimal share, boolean active) {
    }
}
