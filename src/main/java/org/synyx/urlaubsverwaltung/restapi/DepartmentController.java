package org.synyx.urlaubsverwaltung.restapi;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;

import java.util.List;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
@Api(value = "Departments", description = "Get information about the departments of the application")
@Controller("restApiDepartmentController")
public class DepartmentController {

    private static final String ROOT_URL = "/departments";

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "Get all departments of the application", notes = "Get all departments of the application")
    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ModelAttribute("response")
    public List<Department> persons() {

        List<Department> departments = departmentService.getAllDepartments();

        return departments;
    }
}
