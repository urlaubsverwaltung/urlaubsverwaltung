package org.synyx.urlaubsverwaltung.department.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
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
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.department.web.DepartmentDepartmentFormMapper.mapToDepartment;
import static org.synyx.urlaubsverwaltung.department.web.DepartmentDepartmentFormMapper.mapToDepartmentForm;
import static org.synyx.urlaubsverwaltung.department.web.DepartmentDepartmentOverviewDtoMapper.mapToDepartmentOverviewDtos;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping(value = "/web")
public class DepartmentViewController {

    private static final String PERSONS_ATTRIBUTE = "persons";
    private static final String REDIRECT_WEB_DEPARTMENT = "redirect:/web/department/";
    private static final String DEPARTMENT = "department";
    private static final String DEPARTMENT_DEPARTMENT_FORM = "department/department_form";

    private final DepartmentService departmentService;
    private final PersonService personService;
    private final DepartmentViewValidator validator;

    @Autowired
    public DepartmentViewController(DepartmentService departmentService, PersonService personService, DepartmentViewValidator validator) {
        this.departmentService = departmentService;
        this.personService = personService;
        this.validator = validator;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping("/department")
    public String showAllDepartments(Model model) {

        final List<Department> departments = departmentService.getAllDepartments();
        model.addAttribute("departments", mapToDepartmentOverviewDtos(departments));

        final Person signedInUser = personService.getSignedInUser();
        model.addAttribute("canCreateAndModifyDepartment", signedInUser.hasRole(OFFICE));

        return "department/department_list";
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/department/new")
    public String newDepartmentForm(Model model) {

        final List<Person> persons = personService.getActivePersons();

        model.addAttribute(DEPARTMENT, new DepartmentForm());
        model.addAttribute(PERSONS_ATTRIBUTE, persons);

        return DEPARTMENT_DEPARTMENT_FORM;
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/department")
    public String newDepartment(@ModelAttribute(DEPARTMENT) DepartmentForm departmentForm,
                                Errors errors, Model model, RedirectAttributes redirectAttributes) {

        validator.validate(departmentForm, errors);

        if (returnModelErrorAttributes(departmentForm, errors, model)) {
            return DEPARTMENT_DEPARTMENT_FORM;
        }

        final Department createdDepartment = departmentService.create(mapToDepartment(departmentForm));
        final DepartmentForm createdDepartmentForm = mapToDepartmentForm(createdDepartment);

        redirectAttributes.addFlashAttribute("createdDepartment", createdDepartmentForm);

        return REDIRECT_WEB_DEPARTMENT;
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/department/{departmentId}/edit")
    public String editDepartment(@PathVariable("departmentId") Integer departmentId, Model model)
        throws UnknownDepartmentException {

        final Department department = departmentService.getDepartmentById(departmentId)
            .orElseThrow(() -> new UnknownDepartmentException(departmentId));
        model.addAttribute(DEPARTMENT, mapToDepartmentForm(department));

        final List<Person> persons = getInactiveDepartmentMembersAndAllActivePersons(department.getMembers());
        model.addAttribute(PERSONS_ATTRIBUTE, persons);

        return DEPARTMENT_DEPARTMENT_FORM;
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/department/{departmentId}")
    public String updateDepartment(@PathVariable("departmentId") Integer departmentId,
                                   @ModelAttribute(DEPARTMENT) DepartmentForm departmentForm, Errors errors, Model model,
                                   RedirectAttributes redirectAttributes) throws UnknownDepartmentException {

        final Integer persistedDepartmentId = departmentService.getDepartmentById(departmentId)
            .orElseThrow(() -> new UnknownDepartmentException(departmentId)).getId();

        departmentForm.setId(persistedDepartmentId);
        validator.validate(departmentForm, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        if (returnModelErrorAttributes(departmentForm, errors, model)) {
            return DEPARTMENT_DEPARTMENT_FORM;
        }

        final Department updatedDepartment = departmentService.update(mapToDepartment(departmentForm));
        final DepartmentForm updatedDepartmentForm = mapToDepartmentForm(updatedDepartment);

        redirectAttributes.addFlashAttribute("updatedDepartment", updatedDepartmentForm);

        return REDIRECT_WEB_DEPARTMENT;
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/department/{departmentId}/delete")
    public String deleteDepartment(@PathVariable("departmentId") Integer departmentId,
                                   RedirectAttributes redirectAttributes) {

        final Optional<Department> maybeDepartment = departmentService.getDepartmentById(departmentId);
        maybeDepartment.ifPresent(department -> {
            departmentService.delete(department.getId());
            redirectAttributes.addFlashAttribute("deletedDepartment", mapToDepartmentForm(department));
        });

        return REDIRECT_WEB_DEPARTMENT;
    }

    private boolean returnModelErrorAttributes(DepartmentForm departmentForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(DEPARTMENT, departmentForm);

            final List<Person> persons = personService.getActivePersons();
            model.addAttribute(PERSONS_ATTRIBUTE, persons);

            return true;
        }
        return false;
    }

    private List<Person> getInactiveDepartmentMembersAndAllActivePersons(List<Person> departmentMembers) {

        final List<Person> persons = departmentMembers.stream()
            .filter(person -> person.getPermissions().contains(INACTIVE))
            .collect(toList());

        persons.addAll(personService.getActivePersons());

        return persons;
    }
}
