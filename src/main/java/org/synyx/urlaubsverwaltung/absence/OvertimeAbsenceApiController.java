package org.synyx.urlaubsverwaltung.absence;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.synyx.urlaubsverwaltung.absence.AbsenceApiController.ABSENCES;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@Tag(
    name = "absences"
)
@RestControllerAdviceMarker
@RestController
@RequestMapping("/api/persons/{personId}")
public class OvertimeAbsenceApiController {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ApplicationService applicationService;

    @Autowired
    public OvertimeAbsenceApiController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(
        summary = "Returns the amount of overtime used for a specific application of category OVERTIME.",
        description = """
            Returns the amount of overtime used for a specific application of category OVERTIME.
            This includes a list of date-duration tuples, which divides the full duration across the whole application date range.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * user                   - if the requested absences of the person id is from the authenticated user
            * department_head        - if the requested absences of the person id is a managed person of the department head and not of the authenticated user
            * second_stage_authority - if the requested absences of the person id is a managed person of the second stage authority and not of the authenticated user
            * boss or office         - if the requested absences of the person id is any id but not of the authenticated user
            """
    )
    @GetMapping(value = ABSENCES + "/{applicationId}/overtime", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public OvertimeAbsenceDto overtimeAbsence(
        @Parameter(description = "ID of the person")
        @PathVariable("personId")
        Long personId,
        @Parameter(description = "ID of the application")
        @PathVariable("applicationId")
        Long applicationId) {
        Application application = getApplicationOfPerson(personId, applicationId);

        if (!application.getVacationType().isOfCategory(VacationCategory.OVERTIME)) {
            throw new ResponseStatusException(BAD_REQUEST, "Application does not have category OVERTIME.");
        }

        if (application.getHours() == null) {
            LOG.error("hours duration unknown for {}", application);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Application is of category OVERTIME, but duration is not know.");
        }

        return new OvertimeAbsenceDto(applicationId, application.getHours(), application.getOvertimeReductionShares());
    }

    private Application getApplicationOfPerson(Long personId, Long absenceId) {
        return applicationService.getApplicationById(absenceId)
            .filter(application -> application.getPerson().getId().equals(personId))
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No absence for personId=" + personId + " and absenceId=" + absenceId));
    }
}
