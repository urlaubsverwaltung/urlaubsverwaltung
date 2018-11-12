package org.synyx.urlaubsverwaltung.restapi.department;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;

import java.util.stream.Collectors;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
@Api(value = "Departments", description = "Get information about the departments of the application")
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
    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    public ResponseWrapper<DepartmentsListWrapper> departments() {

        return new ResponseWrapper<>(new DepartmentsListWrapper(
                    departmentService.getAllDepartments()
                        .stream()
                        .map(DepartmentResponse::new)
                        .collect(Collectors.toList())));
    }
}
