package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.WITH_AUB;

/**
 * Controller for overview about the sick days of all users.
 */
@Controller
@RequestMapping("/web")
public class SickDaysOverviewViewController {

    private final SickDaysStatisticsService sickDaysStatisticsService;
    private final PersonService personService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    @Autowired
    public SickDaysOverviewViewController(SickDaysStatisticsService sickDaysStatisticsService,
                                          PersonService personService, DateFormatAware dateFormatAware, Clock clock) {

        this.sickDaysStatisticsService = sickDaysStatisticsService;
        this.personService = personService;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_VIEW')")
    @PostMapping("/sickdays/filter")
    public String filterSickNotes(@ModelAttribute("period") FilterPeriod period, Errors errors, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("filterPeriodIncorrect", true);
        }

        final String startDateIsoString = dateFormatAware.formatISO(period.getStartDate());
        final String endDateISoString = dateFormatAware.formatISO(period.getEndDate());

        return "redirect:/web/sickdays?from=" + startDateIsoString + "&to=" + endDateISoString;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_VIEW')")
    @GetMapping("/sickdays")
    public String periodsSickNotes(@RequestParam(value = "from", defaultValue = "") String from,
                                   @RequestParam(value = "to", defaultValue = "") String to,
                                   Model model) {

        final LocalDate startDate = dateFormatAware.parse(from).orElse(null);
        final LocalDate endDate = dateFormatAware.parse(to).orElse(null);
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final Person signedInUser = personService.getSignedInUser();

        final List<SickDaysDetailedStatistics> sickDaysStatistics = sickDaysStatisticsService.getAll(signedInUser, period.getStartDate(), period.getEndDate());

        final List<SickDaysOverviewDto> sickDaysOverviewDtos = sickDaysStatistics.stream()
                .map(statistic -> toSickDaysOverviewDto(statistic, startDate, endDate))
                .collect(toList());

        model.addAttribute("sickDaysStatistics", sickDaysOverviewDtos);
        model.addAttribute("showPersonnelNumberColumn", personnelNumberAvailable(sickDaysStatistics));

        model.addAttribute("today", LocalDate.now(clock));
        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("period", period);

        return "thymeleaf/sicknote/sick_days";
    }

    private static SickDaysOverviewDto toSickDaysOverviewDto(SickDaysDetailedStatistics statistics, LocalDate from, LocalDate to) {

        final Person person = statistics.getPerson();
        final SickDays sickDays = statistics.getSickDays(from, to);
        final SickDays childSickDays = statistics.getChildSickDays(from, to);

        return SickDaysOverviewDto.builder()
            .personId(person.getId())
            .personnelNumber(statistics.getPersonalNumber())
            .personFirstName(person.getFirstName())
            .personLastName(person.getLastName())
            .personNiceName(person.getNiceName())
            .personAvatarUrl(person.getGravatarURL())
            .amountSickDays(sickDays.getDays().get(TOTAL.name()))
            .amountSickDaysWithAUB(sickDays.getDays().get(WITH_AUB.name()))
            .amountChildSickDays(childSickDays.getDays().get(TOTAL.name()))
            .amountChildSickNoteDaysWithAUB(childSickDays.getDays().get(WITH_AUB.name()))
            .build();
    }

    private boolean personnelNumberAvailable(List<SickDaysDetailedStatistics> sickDaysStatistics) {
        return sickDaysStatistics.stream().anyMatch(statistics -> hasText(statistics.getPersonalNumber()));
    }
}
