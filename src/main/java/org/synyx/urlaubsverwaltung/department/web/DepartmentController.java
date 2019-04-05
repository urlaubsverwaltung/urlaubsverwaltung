package org.synyx.urlaubsverwaltung.department.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;

import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping(value = "/web")
public class DepartmentController {

    private static final String PERSONS_ATTRIBUTE = "persons";

    private final DepartmentService departmentService;
    private final PersonService personService;
    private final DepartmentValidator validator;

    @Autowired
    public DepartmentController(DepartmentService departmentService, PersonService personService, DepartmentValidator validator) {
        this.departmentService = departmentService;
        this.personService = personService;
        this.validator = validator;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    @PreAuthorize(SecurityRules.IS_BOSS_OR_OFFICE)
    @GetMapping("/department")
    public String showAllDepartments(Model model) {

        List<Department> departments = departmentService.getAllDepartments();

        model.addAttribute(DepartmentConstants.DEPARTMENTS_ATTRIBUTE, departments);

        return DepartmentConstants.DEPARTMENT_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/department/new")
    public String newDepartmentForm(Model model) {

        List<Person> persons = getPersons();

        model.addAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE, new Department());
        model.addAttribute(PERSONS_ATTRIBUTE, persons);

        return DepartmentConstants.DEPARTMENT_FORM_JSP;
    }


    private List<Person> getPersons() {

        return personService.getActivePersons();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/department")
    public String newDepartment(@ModelAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE) Department department,
        Errors errors, Model model, RedirectAttributes redirectAttributes) {

        validator.validate(department, errors);

        if (errors.hasErrors()) {
            List<Person> persons = getPersons();

            model.addAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE, department);
            model.addAttribute(PERSONS_ATTRIBUTE, persons);

            return DepartmentConstants.DEPARTMENT_FORM_JSP;
        }

        departmentService.create(department);

        redirectAttributes.addFlashAttribute("createdDepartment", department);

        return "redirect:/web/department/";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/department/{departmentId}/edit")
    public String editDepartment(@PathVariable("departmentId") Integer departmentId, Model model)
        throws UnknownDepartmentException {

        Department department = departmentService.getDepartmentById(departmentId).orElseThrow(() ->
                    new UnknownDepartmentException(departmentId));

        List<Person> persons = getPersons();

        model.addAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE, department);
        model.addAttribute(PERSONS_ATTRIBUTE, persons);

        return DepartmentConstants.DEPARTMENT_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/department/{departmentId}")
    public String updateDepartment(@PathVariable("departmentId") Integer departmentId,
        @ModelAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE) Department department, Errors errors, Model model,
        RedirectAttributes redirectAttributes) throws UnknownDepartmentException {

        // Check if department exists
        departmentService.getDepartmentById(departmentId).orElseThrow(() ->
                new UnknownDepartmentException(departmentId));

        validator.validate(department, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
        }

        if (errors.hasErrors()) {
            List<Person> persons = getPersons();

            model.addAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE, department);
            model.addAttribute(PERSONS_ATTRIBUTE, persons);

            return DepartmentConstants.DEPARTMENT_FORM_JSP;
        }

        departmentService.update(department);

        redirectAttributes.addFlashAttribute("updatedDepartment", department);

        return "redirect:/web/department/";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @DeleteMapping("/department/{departmentId}")
    public String deleteDepartment(@PathVariable("departmentId") Integer departmentId,
        RedirectAttributes redirectAttributes) {

        Optional<Department> department = departmentService.getDepartmentById(departmentId);

        departmentService.delete(departmentId);

        department.ifPresent(department1 -> redirectAttributes.addFlashAttribute("deletedDepartment", department1));

        return "redirect:/web/department/";
    }
}
