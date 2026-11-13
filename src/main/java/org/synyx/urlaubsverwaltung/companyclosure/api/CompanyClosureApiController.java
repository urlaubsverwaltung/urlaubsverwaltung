package org.synyx.urlaubsverwaltung.companyclosure.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.synyx.urlaubsverwaltung.period.DateLimits.MAXIMUM_REQUESTED_DURATION;
import static org.synyx.urlaubsverwaltung.period.DateLimits.exceedsMaximumRequestedDuration;

@Tag(
    name = "company closures",
    description = """
        Company Closures: Returns information about company closures (Betriebsruhe)
        """
)
@RestControllerAdviceMarker
@RestController
@RequestMapping("/api")
public class CompanyClosureApiController {

    private final CompanyClosureService companyClosureService;

    CompanyClosureApiController(CompanyClosureService companyClosureService) {
        this.companyClosureService = companyClosureService;
    }

    @Operation(
        summary = "Get all company closures for a certain period",
        description = """
            Returns all company closures for a certain period. Company closures apply to all persons.

            Needed basic authorities:
            * user
            """
    )
    @GetMapping(path = "company-closures", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public CompanyClosuresDto getCompanyClosures(
        @Parameter(description = "Start date with pattern yyyy-MM-dd")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "End date with pattern yyyy-MM-dd, at most 1098 days after the start date")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        if (exceedsMaximumRequestedDuration(startDate, endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Period must not be longer than " + MAXIMUM_REQUESTED_DURATION.toDays() + " days");
        }

        return new CompanyClosuresDto(companyClosureService.getCompanyClosures(startDate, endDate).stream()
            .map(CompanyClosureDto::new)
            .toList());
    }
}
