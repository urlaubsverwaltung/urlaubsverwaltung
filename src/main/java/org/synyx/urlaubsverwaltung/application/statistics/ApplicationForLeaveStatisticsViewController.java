package org.synyx.urlaubsverwaltung.application.statistics;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
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

import static java.lang.Integer.MAX_VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatisticsMapper.mapToApplicationForLeaveStatisticsDto;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;
import static org.synyx.urlaubsverwaltung.web.html.PaginationPageLinkBuilder.buildPageLinkPrefix;

/**
 * Controller to generate applications for leave statistics.
 */
@Controller
@RequestMapping("/web/application/statistics")
class ApplicationForLeaveStatisticsViewController implements HasLaunchpad {

    private final PersonService personService;
    private final ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;
    private final ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;
    private final VacationTypeService vacationTypeService;
    private final DateFormatAware dateFormatAware;
    private final MessageSource messageSource;
    private final Clock clock;

    @Autowired
    ApplicationForLeaveStatisticsViewController(
        PersonService personService, ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService,
        ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService,
        VacationTypeService vacationTypeService, DateFormatAware dateFormatAware, MessageSource messageSource,
        Clock clock) {

        this.personService = personService;
        this.applicationForLeaveStatisticsService = applicationForLeaveStatisticsService;
        this.applicationForLeaveStatisticsCsvExportService = applicationForLeaveStatisticsCsvExportService;
        this.vacationTypeService = vacationTypeService;
        this.dateFormatAware = dateFormatAware;
        this.messageSource = messageSource;
        this.clock = clock;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping
    public String applicationForLeaveStatistics(
        @SortDefault(sort = "person.firstName", direction = Sort.Direction.ASC) Pageable pageable,
        @RequestParam(value = "from", defaultValue = "") String from,
        @RequestParam(value = "to", defaultValue = "") String to,
        @RequestParam(value = "query", required = false, defaultValue = "") String query,
        @RequestHeader(name = "Turbo-Frame", required = false) String turboFrame,
        Model model, Locale locale
    ) {
        final FilterPeriod period = toFilterPeriod(from, to, locale);
        final String pageLinkPrefix = buildPageLinkPrefix(pageable, Map.of("from", period.getStartDateIsoValue(), "to", period.getEndDateIsoValue()));

        final HtmlSelectDto sortSelectDto = sortSelectDto(pageable.getSort());
        model.addAttribute("sortSelect", sortSelectDto);
        model.addAttribute("query", query);

        if (period.startDate().getYear() != period.endDate().getYear()) {
            model.addAttribute("period", period);
            model.addAttribute("errors", "INVALID_PERIOD");
            model.addAttribute("statisticsPagination", new PaginationDto<>(new PageImpl<>(List.of(), pageable, 0), pageLinkPrefix));
            return "application/application-statistics";
        }

        final Person signedInUser = personService.getSignedInUser();
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageable, query);

        final Page<ApplicationForLeaveStatistics> personsPage = applicationForLeaveStatisticsService.getStatistics(signedInUser, period, pageableSearchQuery);

        final List<ApplicationForLeaveStatisticsDto> statisticsDtos = personsPage.stream()
            .map(applicationForLeaveStatistics -> mapToApplicationForLeaveStatisticsDto(applicationForLeaveStatistics, locale, messageSource)).toList();

        final boolean showPersonnelNumberColumn = statisticsDtos.stream()
            .anyMatch(statisticsDto -> hasText(statisticsDto.getPersonnelNumber()));

        final PageImpl<ApplicationForLeaveStatisticsDto> statisticsPage = new PageImpl<>(statisticsDtos, pageable, personsPage.getTotalElements());
        final PaginationDto<ApplicationForLeaveStatisticsDto> statisticsPagination = new PaginationDto<>(statisticsPage, pageLinkPrefix);

        model.addAttribute("statisticsPagination", statisticsPagination);
        model.addAttribute("paginationPageNumbers", IntStream.rangeClosed(1, personsPage.getTotalPages()).boxed().toList());
        model.addAttribute("sortQuery", pageable.getSort().stream().map(order -> order.getProperty() + "," + order.getDirection()).collect(joining("&")));
        model.addAttribute("period", period);
        model.addAttribute("from", period.startDate());
        model.addAttribute("to", period.endDate());
        model.addAttribute("statistics", statisticsDtos);
        model.addAttribute("showPersonnelNumberColumn", showPersonnelNumberColumn);
        model.addAttribute("vacationTypes", vacationTypeDtos(locale));

        final boolean turboFrameRequested = hasText(turboFrame);
        model.addAttribute("turboFrameRequested", turboFrameRequested);

        if (turboFrameRequested) {
            return "application/application-statistics::#" + turboFrame;
        } else {
            return "application/application-statistics";
        }
    }

    private List<ApplicationForLeaveStatisticsVacationTypeDto> vacationTypeDtos(Locale locale) {
        return vacationTypeService.getAllVacationTypes().stream()
            .map(vacationType -> new ApplicationForLeaveStatisticsVacationTypeDto(vacationType.getLabel(locale)))
            .toList();
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping(value = "/download")
    public ResponseEntity<ByteArrayResource> downloadCSV(
        @SortDefault(sort = "person.firstName", direction = Sort.Direction.ASC) Pageable pageable,
        @RequestParam(value = "from", defaultValue = "") String from,
        @RequestParam(value = "to", defaultValue = "") String to,
        @RequestParam(value = "allElements", defaultValue = "false") boolean allElements,
        @RequestParam(value = "query", required = false, defaultValue = "") String query,
        Locale locale, HttpServletResponse response
    ) {
        final FilterPeriod period = toFilterPeriod(from, to, locale);

        // NOTE: Not supported at the moment
        if (period.startDate().getYear() != period.endDate().getYear()) {
            return ResponseEntity.badRequest().build();
        }

        final Person signedInUser = personService.getSignedInUser();

        final Pageable adaptedPageable = allElements ? PageRequest.of(0, MAX_VALUE, pageable.getSort()) : pageable;
        final String adaptedQuery = allElements ? "" : query;
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(adaptedPageable, adaptedQuery);

        final Page<ApplicationForLeaveStatistics> statisticsPage = applicationForLeaveStatisticsService.getStatistics(signedInUser, period, pageableSearchQuery);
        final List<ApplicationForLeaveStatistics> statistics = statisticsPage.getContent();
        final CSVFile csvFile = applicationForLeaveStatisticsCsvExportService.generateCSV(period, locale, statistics);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", UTF_8));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(csvFile.fileName(), UTF_8).build());

        return ResponseEntity.status(OK).headers(headers).body(csvFile.resource());
    }

    private FilterPeriod toFilterPeriod(String startDateString, String endDateString, Locale locale) {
        final LocalDate firstDayOfYear = Year.now(clock).atDay(1);
        final LocalDate startDate = dateFormatAware.parse(startDateString, locale).orElse(firstDayOfYear);
        final LocalDate endDate = dateFormatAware.parse(endDateString, locale).orElseGet(() -> firstDayOfYear.with(lastDayOfYear()));
        return new FilterPeriod(startDate, endDate);
    }

    private static HtmlSelectDto sortSelectDto(Sort originalPersonSort) {

        final List<HtmlOptionDto> personOptions = sortOptionGroupDto("person", List.of("firstName", "lastName"), originalPersonSort);
        final HtmlOptgroupDto personOptgroup = new HtmlOptgroupDto("applications.sort.optgroup.person.label", personOptions);

        final List<HtmlOptionDto> statisticsOptions = sortOptionGroupDto(List.of("totalAllowedVacationDays", "totalWaitingVacationDays", "leftVacationDaysForPeriod", "leftVacationDaysForYear"), originalPersonSort);
        final HtmlOptgroupDto statisticsOptgroup = new HtmlOptgroupDto("applications.sort.optgroup.statistics.label", statisticsOptions);

        return new HtmlSelectDto(List.of(personOptgroup, statisticsOptgroup));
    }

    private static List<HtmlOptionDto> sortOptionGroupDto(List<String> properties, Sort sort) {
        return sortOptionGroupDto("", properties, sort);
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
            new HtmlOptionDto(String.format("applications.statistics.sort.%s.asc", property), prefix + property + ",asc", order != null && order.isAscending()),
            new HtmlOptionDto(String.format("applications.statistics.sort.%s.desc", property), prefix + property + ",desc", order != null && order.isDescending())
        );
    }
}
