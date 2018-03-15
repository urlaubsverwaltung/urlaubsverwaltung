package org.synyx.urlaubsverwaltung.web.statistics;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.statistics.ApplicationForLeaveStatisticsCsvExportService;
import org.synyx.urlaubsverwaltung.core.statistics.ApplicationForLeaveStatisticsService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import liquibase.util.csv.CSVWriter;

/**
 * Controller to generate applications for leave statistics.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping(ApplicationForLeaveStatisticsController.STATISTICS_REL)
public class ApplicationForLeaveStatisticsController {

	static final String STATISTICS_REL = "/web/application/statistics";

	private static final int COLUMN_FIRST_NAME = 1;
	private static final int COLUMN_LAST_NAME = 2;
	private static final int COLUMN_VACATION_DAYS_ALLOWED = 3;
	private static final int COLUMN_VACATION_DAYS_WAITING = 4;
	private static final int COLUMN_VACATION_DAYS_LEFT = 5;
	private static final int COLUMN_VACATION_DAYS_ENTITLEMENT = 6;

	private static final int ORDER_ASCENDING = 0;
	private static final int ORDER_DESCENDING = 1;

	@Autowired
	private ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;

	@Autowired
	private ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;

	@Autowired
	private VacationTypeService vacationTypeService;

	@InitBinder
	public void initBinder(final DataBinder binder) {
		binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
	}

	@PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
	@PostMapping
	public String applicationForLeaveStatistics(@ModelAttribute("period") final FilterPeriod period) {

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("redirect:" + STATISTICS_REL).queryParam("from", period.getStartDateAsString()).queryParam("to",
				period.getEndDateAsString());
		return builder.toUriString();
	}

	@PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
	@GetMapping
	public String applicationForLeaveStatistics(@RequestParam(value = "from", required = false) final String from, @RequestParam(value = "to", required = false) final String to,
			final Model model) {

		FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

		// NOTE: Not supported at the moment
		if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
			model.addAttribute("period", period);
			model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

			return "application/app_statistics";
		}

		List<ApplicationForLeaveStatistics> statistics = applicationForLeaveStatisticsService.getStatistics(period);

		model.addAttribute("from", period.getStartDate());
		model.addAttribute("to", period.getEndDate());
		model.addAttribute("statistics", statistics);
		model.addAttribute("period", period);
		model.addAttribute("vacationTypes", vacationTypeService.getVacationTypes());

		return "application/app_statistics";
	}

	@PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
	@GetMapping(value = "/download")
	public String downloadCSV(@RequestParam(value = "from", required = false) final String from, @RequestParam(value = "to", required = false) final String to,
			@RequestParam(value = "orderColumn", required = false) final int orderColumn, @RequestParam(value = "sortOrder", required = false) final int sortOrder,
			final HttpServletResponse response, final Model model) throws IOException {

		FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

		// NOTE: Not supported at the moment
		if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
			model.addAttribute("period", period);
			model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

			return "application/app_statistics";
		}

		List<ApplicationForLeaveStatistics> statistics = applicationForLeaveStatisticsService.getStatistics(period);

		// Sorting from GUI (JQuery tablesorter)
		if (COLUMN_FIRST_NAME == orderColumn) {
			if (ORDER_ASCENDING == sortOrder) {
				statistics.sort((o1, o2) -> o1.getPerson().getFirstName().compareTo(o2.getPerson().getFirstName()));
			} else if (ORDER_DESCENDING == sortOrder) {
				statistics.sort((o2, o1) -> o1.getPerson().getFirstName().compareTo(o2.getPerson().getFirstName()));
			}
		} else if (COLUMN_LAST_NAME == orderColumn) {
			if (ORDER_ASCENDING == sortOrder) {
				statistics.sort((o1, o2) -> o1.getPerson().getLastName().compareTo(o2.getPerson().getLastName()));
			} else if (ORDER_DESCENDING == sortOrder) {
				statistics.sort((o2, o1) -> o1.getPerson().getLastName().compareTo(o2.getPerson().getLastName()));
			}
		} else if (COLUMN_VACATION_DAYS_ALLOWED == orderColumn) {
			if (ORDER_ASCENDING == sortOrder) {
				statistics.sort((o1, o2) -> o1.getTotalAllowedVacationDays().compareTo(o2.getTotalAllowedVacationDays()));
			} else if (ORDER_DESCENDING == sortOrder) {
				statistics.sort((o2, o1) -> o1.getTotalAllowedVacationDays().compareTo(o2.getTotalAllowedVacationDays()));
			}
		} else if (COLUMN_VACATION_DAYS_WAITING == orderColumn) {
			if (ORDER_ASCENDING == sortOrder) {
				statistics.sort((o1, o2) -> o1.getTotalWaitingVacationDays().compareTo(o2.getTotalWaitingVacationDays()));
			} else if (ORDER_DESCENDING == sortOrder) {
				statistics.sort((o2, o1) -> o1.getTotalWaitingVacationDays().compareTo(o2.getTotalWaitingVacationDays()));
			}
		} else if (COLUMN_VACATION_DAYS_LEFT == orderColumn) {
			if (ORDER_ASCENDING == sortOrder) {
				statistics.sort((o1, o2) -> o1.getLeftVacationDays().compareTo(o2.getLeftVacationDays()));
			} else if (ORDER_DESCENDING == sortOrder) {
				statistics.sort((o2, o1) -> o1.getLeftVacationDays().compareTo(o2.getLeftVacationDays()));
			}
		} else if (COLUMN_VACATION_DAYS_ENTITLEMENT == orderColumn) {
			if (ORDER_ASCENDING == sortOrder) {
				statistics.sort((o1, o2) -> o1.getEntitlementVacationDays().compareTo(o2.getEntitlementVacationDays()));
			} else if (ORDER_DESCENDING == sortOrder) {
				statistics.sort((o2, o1) -> o1.getEntitlementVacationDays().compareTo(o2.getEntitlementVacationDays()));
			}
		}

		String fileName = applicationForLeaveStatisticsCsvExportService.getFileName(period);

		response.setContentType("text/csv");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-disposition", "attachment;filename=" + fileName);

		try (CSVWriter csvWriter = new CSVWriter(response.getWriter())) {
			applicationForLeaveStatisticsCsvExportService.writeStatistics(period, statistics, csvWriter);
		}

		model.addAttribute("period", period);

		return "application/app_statistics";
	}
}
