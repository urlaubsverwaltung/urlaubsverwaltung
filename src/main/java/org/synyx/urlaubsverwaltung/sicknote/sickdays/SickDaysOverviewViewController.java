package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptgroupDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptionDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlSelectDto;
import org.synyx.urlaubsverwaltung.web.html.PaginationDto;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.web.html.PaginationPageLinkBuilder.buildPageLinkPrefix;

/**
 * Controller for overview about the sick days of all users.
 */
@Controller
@RequestMapping("/web")
public class SickDaysOverviewViewController implements HasLaunchpad {

    private final SickDaysStatisticsService sickDaysStatisticsService;
    private final PersonService personService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    @Autowired
    public SickDaysOverviewViewController(
        SickDaysStatisticsService sickDaysStatisticsService,
        PersonService personService,
        DateFormatAware dateFormatAware,
        Clock clock
    ) {
        this.sickDaysStatisticsService = sickDaysStatisticsService;
        this.personService = personService;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_VIEW')")
    @GetMapping("/sickdays")
    public String periodsSickNotes(
        @RequestParam(value = "from", defaultValue = "") String from,
        @RequestParam(value = "to", defaultValue = "") String to,
        @RequestParam(value = "query", required = false, defaultValue = "") String query,
        @SortDefault(sort = "person.firstName", direction = Sort.Direction.ASC) Pageable pageable,
        @RequestHeader(name = "Turbo-Frame", required = false) String turboFrame,
        Model model, Locale locale
    ) {
        final LocalDate firstDayOfYear = Year.now(clock).atDay(1);
        final LocalDate startDate = dateFormatAware.parse(from, locale).orElse(firstDayOfYear);
        final LocalDate endDate = dateFormatAware.parse(to, locale).orElseGet(() -> firstDayOfYear.with(lastDayOfYear()));
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final Person signedInUser = personService.getSignedInUser();

        final Page<SickDaysDetailedStatistics> sickDaysStatisticsPage =
            sickDaysStatisticsService.getAll(signedInUser, period.startDate(), period.endDate(), new PageableSearchQuery(pageable, query));

        model.addAttribute("showPersonnelNumberColumn", personnelNumberAvailable(sickDaysStatisticsPage.getContent()));

        final List<SickDaysOverviewDto> sickDaysOverviewDtos = sickDaysStatisticsPage.stream()
            .map(statistic -> toSickDaysOverviewDto(statistic, period.startDate(), period.endDate()))
            .toList();
        final PageImpl<SickDaysOverviewDto> statisticsPage = new PageImpl<>(sickDaysOverviewDtos, pageable, sickDaysStatisticsPage.getTotalElements());
        final String pageLinkPrefix = buildPageLinkPrefix(sickDaysStatisticsPage.getPageable(), Map.of("from", from, "to", to, "query", query));

        model.addAttribute("statisticsPagination", new PaginationDto<>(statisticsPage, pageLinkPrefix));
        model.addAttribute("paginationPageNumbers", IntStream.rangeClosed(1, sickDaysStatisticsPage.getTotalPages()).boxed().toList());
        model.addAttribute("sortQuery", pageable.getSort().stream().map(order -> order.getProperty() + "," + order.getDirection()).collect(joining("&")));

        final HtmlSelectDto sortSelectDto = sortSelectDto(pageable.getSort());
        model.addAttribute("sortSelect", sortSelectDto);
        model.addAttribute("query", query);

        model.addAttribute("today", LocalDate.now(clock));
        model.addAttribute("from", period.startDate());
        model.addAttribute("to", period.endDate());
        model.addAttribute("period", period);

        final boolean turboFrameRequested = hasText(turboFrame);
        model.addAttribute("turboFrameRequested", turboFrameRequested);

        if (turboFrameRequested) {
            return "sicknote/sick_days::#" + turboFrame;
        } else {
            return "sicknote/sick_days";
        }
    }

    private static HtmlSelectDto sortSelectDto(Sort originalPersonSort) {

        final List<HtmlOptionDto> personOptions = sortOptionGroupDto("person", List.of("firstName", "lastName"), originalPersonSort);
        final HtmlOptgroupDto personOptgroup = new HtmlOptgroupDto("sicknotes.sort.optgroup.person.label", personOptions);

        return new HtmlSelectDto(List.of(personOptgroup));
    }

    private static List<HtmlOptionDto> sortOptionGroupDto(String propertyPrefix, List<String> properties, Sort sort) {
        final List<HtmlOptionDto> options = new ArrayList<>();

        for (String property : properties) {
            final Sort.Order order = sort.getOrderFor(hasText(propertyPrefix) ? propertyPrefix + "." + property : property);
            options.addAll(sortOptionDto(propertyPrefix, property, order));
        }

        return options;
    }

    private static List<HtmlOptionDto> sortOptionDto(String propertyPrefix, String property, Sort.Order order) {
        final String prefix = hasText(propertyPrefix) ? propertyPrefix + "." : "";
        return List.of(
            new HtmlOptionDto(String.format("sicknotes.statistics.sort.%s.asc", property), prefix + property + ",asc", order != null && order.isAscending()),
            new HtmlOptionDto(String.format("sicknotes.statistics.sort.%s.desc", property), prefix + property + ",desc", order != null && order.isDescending())
        );
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
            .personInitials(person.getInitials())
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
