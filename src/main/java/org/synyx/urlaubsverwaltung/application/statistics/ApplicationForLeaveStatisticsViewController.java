package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptgroupDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptionDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlSelectDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatisticsMapper.mapToApplicationForLeaveStatisticsDto;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller to generate applications for leave statistics.
 */
@Controller
@RequestMapping("/web/application/statistics")
class ApplicationForLeaveStatisticsViewController {

    private final PersonService personService;
    private final ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;
    private final ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;
    private final VacationTypeService vacationTypeService;
    private final DateFormatAware dateFormatAware;
    private final MessageSource messageSource;

    @Autowired
    ApplicationForLeaveStatisticsViewController(
        PersonService personService, ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService,
        ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService,
        VacationTypeService vacationTypeService, DateFormatAware dateFormatAware, MessageSource messageSource) {

        this.personService = personService;
        this.applicationForLeaveStatisticsService = applicationForLeaveStatisticsService;
        this.applicationForLeaveStatisticsCsvExportService = applicationForLeaveStatisticsCsvExportService;
        this.vacationTypeService = vacationTypeService;
        this.dateFormatAware = dateFormatAware;
        this.messageSource = messageSource;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping
    public String applicationForLeaveStatistics(@SortDefault.SortDefaults({
                                                    @SortDefault(sort = "person.firstName", direction = Sort.Direction.ASC)
                                                })
                                                Pageable pageable,
                                                @RequestParam(value = "from", defaultValue = "") String from,
                                                @RequestParam(value = "to", defaultValue = "") String to,
                                                @RequestHeader(name = "Turbo-Frame", required = false) String turboFrame,
                                                Model model, Locale locale) {

        final FilterPeriod period = toFilterPeriod(from, to);
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            // TODO model attributes for validation error case
            model.addAttribute("period", period);
            model.addAttribute("errors", "INVALID_PERIOD");
            return "thymeleaf/application/application-statistics";
        }

        final Person signedInUser = personService.getSignedInUser();

        final Page<ApplicationForLeaveStatistics> personsPage = applicationForLeaveStatisticsService.getStatistics(signedInUser, period, pageable);

        final List<ApplicationForLeaveStatisticsDto> statisticsDtos = personsPage.stream()
            .map(applicationForLeaveStatistics -> mapToApplicationForLeaveStatisticsDto(applicationForLeaveStatistics, locale, messageSource)).collect(toList());

        final boolean showPersonnelNumberColumn = statisticsDtos.stream()
            .anyMatch(statisticsDto -> hasText(statisticsDto.getPersonnelNumber()));

        model.addAttribute("statisticsPage", new PageImpl<>(statisticsDtos, pageable, personsPage.getTotalElements()));
        model.addAttribute("paginationPageNumbers", IntStream.rangeClosed(1, personsPage.getTotalPages()).boxed().collect(toList()));
        model.addAttribute("sortQuery", pageable.getSort().stream().map(order -> order.getProperty() + "," + order.getDirection()).collect(toList()).stream().reduce((s, s2) -> s + "&" + s2).orElse(""));
        model.addAttribute("period", period);
        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("statistics", statisticsDtos);
        model.addAttribute("showPersonnelNumberColumn", showPersonnelNumberColumn);
        model.addAttribute("vacationTypes", vacationTypeService.getAllVacationTypes());

        final HtmlSelectDto sortSelectDto = sortSelectDto(pageable.getSort());
        model.addAttribute("sortSelect", sortSelectDto);

        final boolean turboFrameRequested = hasText(turboFrame);
        model.addAttribute("turboFrameRequested", turboFrameRequested);

        if (turboFrameRequested) {
            return "thymeleaf/application/application-statistics::#" + turboFrame;
        } else {
            return "thymeleaf/application/application-statistics";
        }
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping(value = "/download")
    public ResponseEntity<ByteArrayResource> downloadCSV(@SortDefault.SortDefaults({
                                @SortDefault(sort = "person.firstName", direction = Sort.Direction.ASC)
                            })
                            Pageable pageable,
                            @RequestParam(value = "from", defaultValue = "") String from,
                                                         @RequestParam(value = "to", defaultValue = "") String to) {

        final FilterPeriod period = toFilterPeriod(from, to);

        // NOTE: Not supported at the moment
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            return ResponseEntity.badRequest().build();
        }

        final Person signedInUser = personService.getSignedInUser();

        final Page<ApplicationForLeaveStatistics> statisticsPage = applicationForLeaveStatisticsService.getStatistics(signedInUser, period, pageable);
        final List<ApplicationForLeaveStatistics> statistics = statisticsPage.getContent();
        final CSVFile csvFile = applicationForLeaveStatisticsCsvExportService.generateCSV(period, statistics);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(csvFile.getFileName()).build());

        return ResponseEntity.status(OK).headers(headers).body(csvFile.getResource());
    }

    private FilterPeriod toFilterPeriod(String startDateString, String endDateString) {
        final LocalDate startDate = dateFormatAware.parse(startDateString).orElse(null);
        final LocalDate endDate = dateFormatAware.parse(endDateString).orElse(null);
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
