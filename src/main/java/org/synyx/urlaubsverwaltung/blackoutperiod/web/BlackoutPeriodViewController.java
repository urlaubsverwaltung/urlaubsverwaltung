package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriod;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.synyx.urlaubsverwaltung.blackoutperiod.web.BlackoutPeriodFormMapper.mapToBlackoutPeriod;
import static org.synyx.urlaubsverwaltung.blackoutperiod.web.BlackoutPeriodFormMapper.mapToForm;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

/**
 * Controller to manage blackout periods ("Urlaubssperren") - date ranges during which vacation applications
 * are blocked, either company-wide or scoped to specific departments and vacation types.
 */
@Controller
@RequestMapping("/web")
class BlackoutPeriodViewController implements HasLaunchpad {

    private final BlackoutPeriodService blackoutPeriodService;
    private final DepartmentService departmentService;
    private final VacationTypeService vacationTypeService;
    private final BlackoutPeriodFormValidator validator;
    private final MessageSource messageSource;

    BlackoutPeriodViewController(
        BlackoutPeriodService blackoutPeriodService,
        DepartmentService departmentService,
        VacationTypeService vacationTypeService,
        BlackoutPeriodFormValidator validator,
        MessageSource messageSource
    ) {
        this.blackoutPeriodService = blackoutPeriodService;
        this.departmentService = departmentService;
        this.vacationTypeService = vacationTypeService;
        this.validator = validator;
        this.messageSource = messageSource;
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping("/blackoutperiod")
    public String showAllBlackoutPeriods(Model model, Locale locale) {

        final List<BlackoutPeriodListDto> blackoutPeriods = blackoutPeriodService.getAllBlackoutPeriods().stream()
            .map(blackoutPeriod -> mapToListDto(blackoutPeriod, locale))
            .toList();

        model.addAttribute("blackoutPeriods", blackoutPeriods);

        return "blackoutperiod/blackout_period_list";
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping("/blackoutperiod/new")
    public String newBlackoutPeriodForm(Model model, Locale locale) {

        model.addAttribute("blackoutPeriod", new BlackoutPeriodForm());
        prepareFormModel(model, locale, List.of(), List.of());

        return "blackoutperiod/blackout_period_form";
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @PostMapping("/blackoutperiod/new")
    public String createBlackoutPeriod(
        @ModelAttribute("blackoutPeriod") BlackoutPeriodForm form, Errors errors,
        @RequestParam(value = "confirm", required = false, defaultValue = "false") boolean confirm,
        Model model, Locale locale, RedirectAttributes redirectAttributes
    ) {
        return saveBlackoutPeriod(form, errors, confirm, true, model, locale, redirectAttributes, blackoutPeriodService::create);
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping("/blackoutperiod/{id}/edit")
    public String editBlackoutPeriodForm(@PathVariable("id") Long id, Model model, Locale locale) throws UnknownBlackoutPeriodException {

        final BlackoutPeriod blackoutPeriod = blackoutPeriodService.getBlackoutPeriodById(id)
            .orElseThrow(() -> new UnknownBlackoutPeriodException(id));

        model.addAttribute("blackoutPeriod", mapToForm(blackoutPeriod));
        prepareFormModel(model, locale, blackoutPeriod.getDepartments(), blackoutPeriod.getVacationTypes());

        return "blackoutperiod/blackout_period_form";
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @PostMapping("/blackoutperiod/{id}")
    public String updateBlackoutPeriod(
        @PathVariable("id") Long id, @ModelAttribute("blackoutPeriod") BlackoutPeriodForm form, Errors errors,
        @RequestParam(value = "confirm", required = false, defaultValue = "false") boolean confirm,
        Model model, Locale locale, RedirectAttributes redirectAttributes
    ) {
        form.setId(id);
        return saveBlackoutPeriod(form, errors, confirm, false, model, locale, redirectAttributes, blackoutPeriodService::update);
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @PostMapping("/blackoutperiod/{id}/delete")
    public String deleteBlackoutPeriod(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        final Optional<BlackoutPeriod> maybeBlackoutPeriod = blackoutPeriodService.getBlackoutPeriodById(id);
        maybeBlackoutPeriod.ifPresent(blackoutPeriod -> {
            blackoutPeriodService.delete(blackoutPeriod.getId());
            redirectAttributes.addFlashAttribute("deletedBlackoutPeriodTitle", blackoutPeriod.getTitle());
        });

        return "redirect:/web/blackoutperiod";
    }

    private String saveBlackoutPeriod(
        BlackoutPeriodForm form, Errors errors, boolean confirm, boolean isNew,
        Model model, Locale locale, RedirectAttributes redirectAttributes, UnaryOperator<BlackoutPeriod> persist
    ) {

        validator.validate(form, errors);

        final List<Department> allDepartments = departmentService.getAllDepartments();
        final List<VacationType<?>> allVacationTypes = vacationTypeService.getAllVacationTypes();

        if (errors.hasErrors()) {
            prepareFormModel(model, locale, selectedDepartments(form, allDepartments), selectedVacationTypes(form, allVacationTypes));
            return "blackoutperiod/blackout_period_form";
        }

        final BlackoutPeriod blackoutPeriod = mapToBlackoutPeriod(form, allDepartments, allVacationTypes);

        if (!confirm) {
            final List<Application> conflicts = blackoutPeriodService.findConflictingApplications(blackoutPeriod);
            if (!conflicts.isEmpty()) {
                model.addAttribute("conflictingApplications", mapToConflictDtos(conflicts, locale));
                model.addAttribute("confirmRequired", true);
                prepareFormModel(model, locale, blackoutPeriod.getDepartments(), blackoutPeriod.getVacationTypes());
                return "blackoutperiod/blackout_period_form";
            }
        }

        final BlackoutPeriod savedBlackoutPeriod = persist.apply(blackoutPeriod);
        redirectAttributes.addFlashAttribute(isNew ? "createdBlackoutPeriodTitle" : "updatedBlackoutPeriodTitle", savedBlackoutPeriod.getTitle());

        return "redirect:/web/blackoutperiod";
    }

    private void prepareFormModel(Model model, Locale locale, List<Department> selectedDepartments, List<VacationType<?>> selectedVacationTypes) {

        final Set<Long> selectedDepartmentIds = selectedDepartments.stream().map(Department::getId).collect(toSet());
        final Set<Long> selectedVacationTypeIds = selectedVacationTypes.stream().map(VacationType::getId).collect(toSet());

        final List<BlackoutPeriodOptionDto> departmentOptions = departmentService.getAllDepartments().stream()
            .map(department -> new BlackoutPeriodOptionDto(department.getId(), department.getName(), selectedDepartmentIds.contains(department.getId())))
            .toList();

        final List<BlackoutPeriodOptionDto> vacationTypeOptions = vacationTypeService.getActiveVacationTypes().stream()
            .map(vacationType -> new BlackoutPeriodOptionDto(vacationType.getId(), vacationType.getLabel(locale), selectedVacationTypeIds.contains(vacationType.getId())))
            .toList();

        model.addAttribute("departmentOptions", departmentOptions);
        model.addAttribute("vacationTypeOptions", vacationTypeOptions);
    }

    private static List<Department> selectedDepartments(BlackoutPeriodForm form, List<Department> allDepartments) {
        final Set<Long> selectedIds = Set.copyOf(form.getDepartmentIds());
        return allDepartments.stream().filter(department -> selectedIds.contains(department.getId())).toList();
    }

    private static List<VacationType<?>> selectedVacationTypes(BlackoutPeriodForm form, List<VacationType<?>> allVacationTypes) {
        final Set<Long> selectedIds = Set.copyOf(form.getVacationTypeIds());
        return allVacationTypes.stream().filter(vacationType -> selectedIds.contains(vacationType.getId())).toList();
    }

    private BlackoutPeriodListDto mapToListDto(BlackoutPeriod blackoutPeriod, Locale locale) {

        final String scopeLabel = blackoutPeriod.isCompanyWide()
            ? messageSource.getMessage("blackoutperiod.scope.companyWide", new Object[]{}, locale)
            : blackoutPeriod.getDepartments().stream().map(Department::getName).collect(joining(", "));

        final String vacationTypesLabel = blackoutPeriod.appliesToAllVacationTypes()
            ? messageSource.getMessage("blackoutperiod.scope.allVacationTypes", new Object[]{}, locale)
            : blackoutPeriod.getVacationTypes().stream().map(vacationType -> vacationType.getLabel(locale)).collect(joining(", "));

        return new BlackoutPeriodListDto(blackoutPeriod.getId(), blackoutPeriod.getTitle(), blackoutPeriod.getStartDate(),
            blackoutPeriod.getEndDate(), scopeLabel, vacationTypesLabel);
    }

    private static List<BlackoutPeriodConflictDto> mapToConflictDtos(List<Application> applications, Locale locale) {
        return applications.stream()
            .map(application -> new BlackoutPeriodConflictDto(
                application.getPerson().getNiceName(),
                application.getStartDate(),
                application.getEndDate(),
                application.getVacationType().getLabel(locale)))
            .toList();
    }
}
