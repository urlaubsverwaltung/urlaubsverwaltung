package org.synyx.urlaubsverwaltung.department.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.department.web.DepartmentDepartmentFormMapper.mapToDepartment;
import static org.synyx.urlaubsverwaltung.department.web.DepartmentDepartmentFormMapper.mapToDepartmentForm;
import static org.synyx.urlaubsverwaltung.department.web.DepartmentDepartmentOverviewDtoMapper.mapToDepartmentOverviewDtos;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.web.HotwiredTurboConstants.TURBO_STREAM_MEDIA_TYPE;

@Controller
@RequestMapping(value = "/web")
public class DepartmentViewController implements HasLaunchpad {

    private final DepartmentService departmentService;
    private final PersonService personService;
    private final DepartmentViewValidator validator;

    @Autowired
    DepartmentViewController(DepartmentService departmentService, PersonService personService, DepartmentViewValidator validator) {
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
        model.addAttribute("persons", persons);
        model.addAttribute("department", new DepartmentForm());

        return "department/department_form";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/department/new")
    public String newDepartment(@ModelAttribute("department") DepartmentForm departmentForm, Errors errors,
                                Model model, RedirectAttributes redirectAttributes) {

        return createNewDepartment(departmentForm, errors, model,
            () -> "department/department_form",
            department -> {
                redirectAttributes.addFlashAttribute("createdDepartmentName", department.getName());
                return "redirect:/web/department";
            });
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping(value = "/department/new", produces = TURBO_STREAM_MEDIA_TYPE)
    public ModelAndView newDepartmentAjax(@ModelAttribute("department") DepartmentForm departmentForm, Errors errors,
                                          Model model, RedirectAttributes redirectAttributes) {

        return createNewDepartment(departmentForm, errors, model,
            () -> new ModelAndView("department/department_form", model.asMap(), UNPROCESSABLE_ENTITY),
            department -> {
                redirectAttributes.addFlashAttribute("createdDepartmentName", department.getName());
                return new ModelAndView("redirect:/web/department");
            });
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/department/{departmentId}/edit")
    public String editDepartment(@PathVariable("departmentId") Long departmentId, Model model)
        throws UnknownDepartmentException {

        final Department department = departmentService.getDepartmentById(departmentId)
            .orElseThrow(() -> new UnknownDepartmentException(departmentId));
        model.addAttribute("department", mapToDepartmentForm(department));

        final List<Person> persons = getDepartmentMembersAndAllActivePersons(department.getMembers());
        model.addAttribute("persons", persons);
        model.addAttribute("hiddenDepartmentMembers", List.of());
        model.addAttribute("hiddenDepartmentHeads", List.of());
        model.addAttribute("hiddenDepartmentSecondStageAuthorities", List.of());

        return "department/department_form";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/department/{departmentId}")
    public String updateDepartment(@PathVariable("departmentId") Long departmentId,
                                   @ModelAttribute("department") DepartmentForm departmentForm, Errors errors,
                                   Model model, RedirectAttributes redirectAttributes) {

        return editDepartment(departmentForm, errors, model,
            () -> "department/department_form",
            department -> {
                redirectAttributes.addFlashAttribute("updatedDepartmentName", department.getName());
                return "redirect:/web/department";
            });
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping(value = "/department/{departmentId}", produces = TURBO_STREAM_MEDIA_TYPE)
    public ModelAndView updateDepartmentAjax(@PathVariable("departmentId") Long departmentId,
                                             @ModelAttribute("department") DepartmentForm departmentForm, Errors errors,
                                             Model model, RedirectAttributes redirectAttributes) {

        return editDepartment(departmentForm, errors, model,
            () -> new ModelAndView("department/department_form", model.asMap(), UNPROCESSABLE_ENTITY),
            department -> {
                redirectAttributes.addFlashAttribute("createdDepartmentName", department.getName());
                return new ModelAndView("redirect:/web/department");
            });
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping(value = {"/department/new", "/department/{departmentId}/edit"}, params = "do-member-search")
    public String updateDepartment(@PathVariable(value = "departmentId", required = false) Long departmentId,
                                   @RequestParam("memberQuery") String memberQuery,
                                   @ModelAttribute("department") DepartmentForm departmentForm,
                                   @RequestHeader(name = "Turbo-Frame", required = false) String turboFrame,
                                   Model model) throws UnknownDepartmentException {

        final List<Person> allPersons;
        if (departmentId == null) {
            allPersons = personService.getActivePersons();
        } else {
            final Department department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new UnknownDepartmentException(departmentId));
            allPersons = getDepartmentMembersAndAllActivePersons(department.getMembers());
        }

        final List<Person> persons = hasText(memberQuery)
            ? filter(allPersons, person -> person.getNiceName().toLowerCase().contains(memberQuery.toLowerCase()))
            : allPersons;

        model.addAttribute("department", departmentForm);
        model.addAttribute("persons", persons);
        model.addAttribute("hiddenDepartmentMembers", filter(departmentForm.getMembers(), not(persons::contains)));
        model.addAttribute("hiddenDepartmentHeads", filter(departmentForm.getDepartmentHeads(), not(persons::contains)));
        model.addAttribute("hiddenDepartmentSecondStageAuthorities", filter(departmentForm.getSecondStageAuthorities(), not(persons::contains)));
        model.addAttribute("memberQuery", memberQuery);

        final boolean turboFrameRequested = hasText(turboFrame);
        model.addAttribute("turboFrameRequested", turboFrameRequested);

        if (turboFrameRequested) {
            return "department/department_form::#" + turboFrame;
        } else {
            return "department/department_form";
        }
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/department/{departmentId}/delete")
    public String deleteDepartment(@PathVariable("departmentId") Long departmentId,
                                   RedirectAttributes redirectAttributes) {

        final Optional<Department> maybeDepartment = departmentService.getDepartmentById(departmentId);
        maybeDepartment.ifPresent(department -> {
            departmentService.delete(department.getId());
            redirectAttributes.addFlashAttribute("deletedDepartmentName", department.getName());
        });

        return "redirect:/web/department";
    }

    private <T> T createNewDepartment(DepartmentForm departmentForm, Errors errors, Model model,
                                      Supplier<T> errorReturn, Function<Department, T> successReturn) {

        return createOrEditDepartment(departmentForm, errors, model, departmentService::create, errorReturn, successReturn);
    }

    private <T> T editDepartment(DepartmentForm departmentForm, Errors errors, Model model,
                                 Supplier<T> errorReturn, Function<Department, T> successReturn) {

        return createOrEditDepartment(departmentForm, errors, model, departmentService::update, errorReturn, successReturn);
    }

    private <T> T createOrEditDepartment(DepartmentForm departmentForm, Errors errors, Model model,
                                         UnaryOperator<Department> departmentFunction,
                                         Supplier<T> errorReturn, Function<Department, T> successReturn) {

        validator.validate(departmentForm, errors);
        if (errors.hasErrors()) {

            final List<Person> persons = getDepartmentMembersAndAllActivePersons(departmentForm.getMembers());
            model.addAttribute("persons", persons);

            return errorReturn.get();
        }

        final Department department = departmentFunction.apply(mapToDepartment(departmentForm));
        return successReturn.apply(department);
    }

    private List<Person> getDepartmentMembersAndAllActivePersons(List<Person> departmentMembers) {

        final List<Person> sortedDepartmentMembers = departmentMembers
            .stream()
            .sorted((o1, o2) -> o1.getNiceName().compareToIgnoreCase(o2.getNiceName()))
            .toList();

        // sort department members to the top of the list shown in ui.
        return Stream.of(sortedDepartmentMembers, personService.getActivePersons())
            .flatMap(Collection::stream)
            .distinct()
            .toList();
    }

    private static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).toList();
    }
}
