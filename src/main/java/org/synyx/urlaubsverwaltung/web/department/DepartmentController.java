package org.synyx.urlaubsverwaltung.web.department;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.validator.DepartmentValidator;

import java.util.List;
import java.util.Optional;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
@Controller
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private DepartmentValidator validator;

    @RequestMapping(value = "/department", method = RequestMethod.GET)
    public String showAllDepartments(Model model) {

        if (sessionService.isOffice() || sessionService.isBoss()) {
            List<Department> departments = departmentService.getAllDepartments();

            model.addAttribute("departments", departments);

            return DepartmentConstants.DEPARTMENT_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    @RequestMapping(value = "/department/new", method = RequestMethod.GET)
    public String newDepartmentForm(Model model) {

        if (!sessionService.isOffice()) {
            return ControllerConstants.ERROR_JSP;
        }

        model.addAttribute("department", new Department());

        return DepartmentConstants.DEPARTMENT_FORM_JSP;
    }


    @RequestMapping(value = "/department", method = RequestMethod.POST)
    public String newDepartment(@ModelAttribute("department") Department department, Errors errors, Model model) {

        if (!sessionService.isOffice()) {
            return ControllerConstants.ERROR_JSP;
        }

        // validate department
        validator.validate(department, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        if (errors.hasErrors()) {
            model.addAttribute("department", department);

            return DepartmentConstants.DEPARTMENT_FORM_JSP;
        }

        departmentService.create(department);

        return "redirect:/web/department/";
    }


    @RequestMapping(value = "/department/{departmentId}/edit", method = RequestMethod.GET)
    public String editDepartment(@PathVariable("departmentId") Integer departmentId, Model model) {

        if (!sessionService.isOffice()) {
            return ControllerConstants.ERROR_JSP;
        }

        Optional<Department> optionalDepartment = departmentService.getDepartmentById(departmentId);

        if (!optionalDepartment.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        Department department = optionalDepartment.get();

        model.addAttribute("department", department);

        return DepartmentConstants.DEPARTMENT_FORM_JSP;
    }


    @RequestMapping(value = "/department/{departmentId}", method = RequestMethod.PUT)
    public String updateDepartement(@PathVariable("departmentId") Integer departmentId,
        @ModelAttribute("department") Department department, Errors errors, Model model) {

        Optional<Department> departmentToUpdate = departmentService.getDepartmentById(departmentId);

        if (!sessionService.isOffice() || !departmentToUpdate.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        validator.validate(department, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        if (errors.hasErrors()) {
            model.addAttribute("department", department);

            return DepartmentConstants.DEPARTMENT_FORM_JSP;
        }

        departmentService.update(department);

        return "redirect:/web/department/";
    }


    @RequestMapping(value = "/department/{departmentId}", method = RequestMethod.DELETE)
    public String deleteDepartment(@PathVariable("departmentId") Integer departmentId) {

        if (!sessionService.isOffice()) {
            return ControllerConstants.ERROR_JSP;
        }

        departmentService.delete(departmentId);

        return "redirect:/web/department/";
    }
}
