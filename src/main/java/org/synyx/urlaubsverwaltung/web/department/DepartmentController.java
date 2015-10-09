package org.synyx.urlaubsverwaltung.web.department;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
@Controller
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PersonService personService;

    @Autowired
    private DepartmentValidator validator;

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    @PreAuthorize(SecurityRules.IS_BOSS_OR_OFFICE)
    @RequestMapping(value = "/department", method = RequestMethod.GET)
    public String showAllDepartments(Model model) {

        List<Department> departments = departmentService.getAllDepartments();

        model.addAttribute(DepartmentConstants.DEPARTMENTS_ATTRIBUTE, departments);

        return DepartmentConstants.DEPARTMENT_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/department/new", method = RequestMethod.GET)
    public String newDepartmentForm(Model model) {

        List<Person> persons = getPersons();
        Map<Person, String> gravatarUrls = PersonConstants.getGravatarURLs(persons);

        model.addAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE, new Department());
        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
        model.addAttribute(PersonConstants.GRAVATAR_URLS_ATTRIBUTE, gravatarUrls);

        return DepartmentConstants.DEPARTMENT_FORM_JSP;
    }


    private List<Person> getPersons() {

        return personService.getActivePersons().stream().sorted(personComparator()).collect(Collectors.toList());
    }


    private Comparator<Person> personComparator() {

        return (p1, p2) -> p1.getNiceName().toLowerCase().compareTo(p2.getNiceName().toLowerCase());
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/department", method = RequestMethod.POST)
    public String newDepartment(@ModelAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE) Department department,
        Errors errors, Model model, RedirectAttributes redirectAttributes) {

        validator.validate(department, errors);

        if (errors.hasErrors()) {
            List<Person> persons = getPersons();
            Map<Person, String> gravatarUrls = PersonConstants.getGravatarURLs(persons);

            model.addAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE, department);
            model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
            model.addAttribute(PersonConstants.GRAVATAR_URLS_ATTRIBUTE, gravatarUrls);

            return DepartmentConstants.DEPARTMENT_FORM_JSP;
        }

        departmentService.create(department);

        redirectAttributes.addFlashAttribute("createdDepartment", department);

        return "redirect:/web/department/";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/department/{departmentId}/edit", method = RequestMethod.GET)
    public String editDepartment(@PathVariable("departmentId") Integer departmentId, Model model) {

        Optional<Department> optionalDepartment = departmentService.getDepartmentById(departmentId);

        if (!optionalDepartment.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        Department department = optionalDepartment.get();
        List<Person> persons = getPersons();
        Map<Person, String> gravatarUrls = PersonConstants.getGravatarURLs(persons);

        model.addAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE, department);
        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
        model.addAttribute(PersonConstants.GRAVATAR_URLS_ATTRIBUTE, gravatarUrls);

        return DepartmentConstants.DEPARTMENT_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/department/{departmentId}", method = RequestMethod.PUT)
    public String updateDepartment(@PathVariable("departmentId") Integer departmentId,
        @ModelAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE) Department department, Errors errors, Model model,
        RedirectAttributes redirectAttributes) {

        Optional<Department> departmentToUpdate = departmentService.getDepartmentById(departmentId);

        if (!departmentToUpdate.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        validator.validate(department, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
        }

        if (errors.hasErrors()) {
            List<Person> persons = getPersons();
            Map<Person, String> gravatarUrls = PersonConstants.getGravatarURLs(persons);

            model.addAttribute(DepartmentConstants.DEPARTMENT_ATTRIBUTE, department);
            model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
            model.addAttribute(PersonConstants.GRAVATAR_URLS_ATTRIBUTE, gravatarUrls);

            return DepartmentConstants.DEPARTMENT_FORM_JSP;
        }

        departmentService.update(department);

        redirectAttributes.addFlashAttribute("updatedDepartment", department);

        return "redirect:/web/department/";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/department/{departmentId}", method = RequestMethod.DELETE)
    public String deleteDepartment(@PathVariable("departmentId") Integer departmentId,
        RedirectAttributes redirectAttributes) {

        Optional<Department> department = departmentService.getDepartmentById(departmentId);

        departmentService.delete(departmentId);

        if (department.isPresent()) {
            redirectAttributes.addFlashAttribute("deletedDepartment", department.get());
        }

        return "redirect:/web/department/";
    }
}
