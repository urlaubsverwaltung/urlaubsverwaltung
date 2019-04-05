package org.synyx.urlaubsverwaltung.staff.web;

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
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.department.web.DepartmentConstants;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;


@Controller
@RequestMapping("/web")
public class StaffManagementController {

    private static final String PERSON_FORM_JSP = "person/person_form";
    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final PersonValidator validator;

    @Autowired
    public StaffManagementController(PersonService personService, DepartmentService departmentService, PersonValidator validator) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.validator = validator;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/staff/new")
    public String newPersonForm(Model model) {

        model.addAttribute(PERSON_ATTRIBUTE, new Person());

        return PERSON_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/staff")
    public String newPerson(@ModelAttribute(PERSON_ATTRIBUTE) Person person,
                            Errors errors,
                            RedirectAttributes redirectAttributes) {

        validator.validate(person, errors);

        if (errors.hasErrors()) {
            return PERSON_FORM_JSP;
        }

        Person createdPerson = personService.create(person);

        redirectAttributes.addFlashAttribute("createSuccess", true);

        return "redirect:/web/staff/" + createdPerson.getId();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/staff/{personId}/edit")
    public String editPersonForm(@PathVariable("personId") Integer personId,
                                 Model model)
        throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        model.addAttribute(PERSON_ATTRIBUTE, person);
        model.addAttribute(DepartmentConstants.DEPARTMENTS_ATTRIBUTE,
                                departmentService.getManagedDepartmentsOfDepartmentHead(person));
        model.addAttribute(DepartmentConstants.SECOND_STAGE_DEPARTMENTS_ATTRIBUTE,
                departmentService.getManagedDepartmentsOfSecondStageAuthority(person));

        return PERSON_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/staff/{personId}/edit")
    public String editPerson(@PathVariable("personId") Integer personId,
        @ModelAttribute(PERSON_ATTRIBUTE) Person person, Errors errors,
        RedirectAttributes redirectAttributes) {

        validator.validate(person, errors);

        if (errors.hasErrors()) {
            return PERSON_FORM_JSP;
        }

        personService.update(person);

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/staff/" + personId;
    }
}
