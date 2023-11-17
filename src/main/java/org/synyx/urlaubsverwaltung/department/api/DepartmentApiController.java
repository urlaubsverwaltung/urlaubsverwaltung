package org.synyx.urlaubsverwaltung.department.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import java.util.List;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Tag(
    name = "departments",
    description = """
        Departments: Returns information about the departments
        """
)
@RestControllerAdviceMarker
@RestController
@RequestMapping("/api")
public class DepartmentApiController {

    private final DepartmentService departmentService;

    @Autowired
    DepartmentApiController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(
        summary = "Returns all departments",
        description = """
            Returns all departments.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * office
            """)
    @GetMapping(value = "/departments", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    @PreAuthorize(IS_OFFICE)
    public ResponseEntity<DepartmentsDto> departments() {

        final List<DepartmentDto> departments = departmentService.getAllDepartments()
            .stream()
            .map(DepartmentDto::new)
            .toList();

        return new ResponseEntity<>(new DepartmentsDto(departments), OK);
    }
}
