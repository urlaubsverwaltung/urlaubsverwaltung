package org.synyx.urlaubsverwaltung.restapi.department;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;

import java.util.stream.Collectors;


@Api("Departments: Get information about the departments of the application")
@RestController("restApiDepartmentController")
@RequestMapping("/api")
public class DepartmentController {

    private static final String ROOT_URL = "/departments";

    private final DepartmentService departmentService;

    @Autowired
    DepartmentController(DepartmentService departmentService) {

        this.departmentService = departmentService;
    }

    @ApiOperation(value = "Get all departments of the application", notes = "Get all departments of the application")
    @GetMapping(ROOT_URL)
    public ResponseWrapper<DepartmentsListWrapper> departments() {

        return new ResponseWrapper<>(new DepartmentsListWrapper(
            departmentService.getAllDepartments()
                .stream()
                .map(DepartmentResponse::new)
                .collect(Collectors.toList())));
    }
}
