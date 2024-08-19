package org.synyx.urlaubsverwaltung.absence;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.*;
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
        summary = "Returns the amount of overtime used for a specific absence of category OVERTIME",
        description = """
            Returns the amount of overtime used for a specific absence of category OVERTIME.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * user                   - if the requested absences of the person id is from the authenticated user
            * department_head        - if the requested absences of the person id is a managed person of the department head and not of the authenticated user
            * second_stage_authority - if the requested absences of the person id is a managed person of the second stage authority and not of the authenticated user
            * boss or office         - if the requested absences of the person id is any id but not of the authenticated user
            """
    )
    @GetMapping(value = ABSENCES + "/{absenceId}/overtime", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public OvertimeAbsenceDto overtimeAbsence(
        @Parameter(description = "ID of the person")
        @PathVariable("personId")
        Long personId,
        @Parameter(description = "ID of the absence")
        @PathVariable("absenceId")
        Long absenceId) {
        ResponseStatusException notFoundException = new ResponseStatusException(NOT_FOUND, "No absence for personId=" + personId + " and absenceId=" + absenceId);
        Application application = applicationService.getApplicationById(absenceId).orElseThrow(() -> notFoundException);

        if (!application.getPerson().getId().equals(personId)) {
            throw notFoundException;
        }

        if (!application.getVacationType().isOfCategory(VacationCategory.OVERTIME)) {
            throw new ResponseStatusException(BAD_REQUEST, "Absence does not have category OVERTIME.");
        }

        if (application.getHours() == null) {
            LOG.error("hours duration unknown for {}", application);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Absence is of category OVERTIME, but usedOvertimeDuration is not know.");
        }

        return new OvertimeAbsenceDto(absenceId, application.getHours());
    }
}
