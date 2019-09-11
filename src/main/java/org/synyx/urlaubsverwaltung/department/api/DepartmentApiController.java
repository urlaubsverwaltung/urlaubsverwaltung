package org.synyx.urlaubsverwaltung.department.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;

import java.util.List;

import static java.util.stream.Collectors.toList;


@Api("Departments: Get information about the departments of the application")
@RestController("restApiDepartmentController")
@RequestMapping("/api")
public class DepartmentApiController {

    private final DepartmentService departmentService;

    @Autowired
    DepartmentApiController(DepartmentService departmentService) {

        this.departmentService = departmentService;
    }

    @ApiOperation(value = "Get all departments of the application", notes = "Get all departments of the application")
    @GetMapping("/departments")
    @PreAuthorize(SecurityRules.IS_OFFICE)
    public ResponseWrapper<DepartmentsListWrapper> departments() {

        final List<DepartmentResponse> departmentResponses = departmentService.getAllDepartments()
            .stream()
            .map(DepartmentResponse::new)
            .collect(toList());

        return new ResponseWrapper<>(new DepartmentsListWrapper(departmentResponses));
    }
}
