package org.synyx.urlaubsverwaltung.absence.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.absence.AbsencePeriod;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@RequestMapping("/web/absences")
@Controller
public class AbsenceOverviewViewController implements HasLaunchpad {

    private static final VacationTypeColor ANONYMIZED_ABSENCE_COLOR = VacationTypeColor.YELLOW;

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final MessageSource messageSource;
    private final Clock clock;
    private final PublicHolidaysService publicHolidaysService;
    private final AbsenceService absenceService;
    private final WorkingTimeService workingTimeService;
    private final VacationTypeService vacationTypeService;

    @Autowired
    public AbsenceOverviewViewController(
        PersonService personService, DepartmentService departmentService,
        MessageSource messageSource, Clock clock,
        PublicHolidaysService publicHolidaysService,
        AbsenceService absenceService, WorkingTimeService workingTimeService,
        VacationTypeService vacationTypeService
    ) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.messageSource = messageSource;
        this.clock = clock;
        this.publicHolidaysService = publicHolidaysService;
        this.absenceService = absenceService;
        this.workingTimeService = workingTimeService;
        this.vacationTypeService = vacationTypeService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(List.class, new CustomCollectionEditor(List.class));
    }

    @GetMapping
    public String absenceOverview(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String month,
        @RequestParam(name = "department", required = false, defaultValue = "") List<String> rawSelectedDepartments, Model model, Locale locale) {

        final Person signedInUser = personService.getSignedInUser();

        final List<Person> overviewPersons;
        if (departmentService.getNumberOfDepartments() > 0) {

            final List<Department> visibleDepartments = departmentService.getDepartmentsPersonHasAccessTo(signedInUser);
            model.addAttribute("visibleDepartments", visibleDepartments);

            if (visibleDepartments.isEmpty()) {
                overviewPersons = List.of(signedInUser);
            } else {
                final List<String> selectedDepartmentNames = getSelectedDepartmentNames(rawSelectedDepartments, visibleDepartments);
                model.addAttribute("selectedDepartments", selectedDepartmentNames);

                overviewPersons = visibleDepartments.stream()
                    .filter(department -> selectedDepartmentNames.contains(department.getName()))
                    .map(Department::getMembers)
                    .flatMap(List::stream)
                    .filter(member -> !member.hasRole(INACTIVE))
                    .distinct()
                    .sorted(comparing(Person::getFirstName))
                    .toList();
            }
        } else {
            overviewPersons = personService.getActivePersons();
        }

        final LocalDate startDate = getStartDate(year, month);
        final LocalDate endDate = getEndDate(year, month);

        model.addAttribute("currentYear", Year.now(clock).getValue());
        model.addAttribute("selectedYear", startDate.getYear());

        final String selectedMonth = getSelectedMonth(month, startDate);
        model.addAttribute("selectedMonth", selectedMonth);

        final List<Person> membersOfSignedInUser = getActiveMembersOfPerson(signedInUser);
        final boolean isSignedInUserAllowedToSeeAbsencesOfOthers = !membersOfSignedInUser.isEmpty();
        model.addAttribute("sickNoteLegendVisible", isSignedInUserAllowedToSeeAbsencesOfOthers || overviewPersons.contains(signedInUser));

        final List<VacationType<?>> vacationTypes = vacationTypeService.getAllVacationTypes();
        final Map<Long, VacationType<?>> vacationTypesById = vacationTypes.stream().collect(toMap(VacationType::getId, Function.identity()));

        final boolean isSignedInUserInOverview = overviewPersons.contains(signedInUser);
        // use active vacation types instead of all to avoid too many items in the legend.
        // (there could be non-active vacation types visible in the absence-overview)
        // the legend will be removed soon -> therefore display just the active items
        List<VacationTypeColorDto> vacationTypeColorDtos = prepareVacationTypeColorsForLegend(isSignedInUserAllowedToSeeAbsencesOfOthers, isSignedInUserInOverview, vacationTypes, locale);
        model.addAttribute("vacationTypeColors", vacationTypeColorDtos);

        final Function<AbsencePeriod.RecordInfo, Boolean> shouldAnonymizeAbsenceType = recordInfo -> !recordInfo.getPerson().equals(signedInUser)
            && !membersOfSignedInUser.contains(recordInfo.getPerson()) && !recordInfo.isVisibleToEveryone();

        final Function<AbsencePeriod.RecordInfo, VacationTypeColor> recordInfoToColor = recordInfo -> recordInfoToColor(recordInfo, vacationTypesById::get);

        final DateRange dateRange = new DateRange(startDate, endDate);
        final List<AbsenceOverviewMonthDto> months = getAbsenceOverViewMonthModels(dateRange, overviewPersons, locale, shouldAnonymizeAbsenceType, recordInfoToColor);
        final AbsenceOverviewDto absenceOverview = new AbsenceOverviewDto(months);
        model.addAttribute("absenceOverview", absenceOverview);

        return "absences/absences-overview";
    }

    private List<VacationTypeColorDto> prepareVacationTypeColorsForLegend(boolean isSignedInUserAllowedToSeeAbsences, boolean isSignedInUserInOverview, List<VacationType<?>> vacationTypes, Locale locale) {

        List<VacationTypeColorDto> vacationTypeColorDtos;

        final Function<VacationType<?>, VacationTypeColorDto> toVacationTypeColorsDto = vacationType -> toVacationTypeColorsDto(vacationType, locale);
        final List<VacationType<?>> activeVacationTypes = vacationTypes.stream().filter(VacationType::isActive).toList();

        if (isSignedInUserAllowedToSeeAbsences) {
            vacationTypeColorDtos = activeVacationTypes.stream().map(toVacationTypeColorsDto).toList();
        } else {
            if (isSignedInUserInOverview) {
                vacationTypeColorDtos = activeVacationTypes.stream().map(toVacationTypeColorsDto).collect(Collectors.toList());
            } else {
                vacationTypeColorDtos = activeVacationTypes.stream().filter(VacationType::isVisibleToEveryone).map(toVacationTypeColorsDto).collect(Collectors.toList());
            }
            if (activeVacationTypes.stream().anyMatch(vacationType -> !vacationType.isVisibleToEveryone())) {
                vacationTypeColorDtos.add(getAnonymizedAbsenceTypeColor(locale));
            }
        }
        return vacationTypeColorDtos;
    }

    private List<String> getSelectedDepartmentNames(List<String> rawSelectedDepartments, List<Department> departments) {
        final List<String> preparedSelectedDepartments = rawSelectedDepartments.stream().filter(StringUtils::hasText).toList();
        return preparedSelectedDepartments.isEmpty() ? List.of(departments.getFirst().getName()) : preparedSelectedDepartments;
    }

    private List<AbsenceOverviewMonthDto> getAbsenceOverViewMonthModels(DateRange dateRange,
                                                                        List<Person> personList,
                                                                        Locale locale,
                                                                        Function<AbsencePeriod.RecordInfo, Boolean> shouldAnonymizeAbsenceType,
                                                                        Function<AbsencePeriod.RecordInfo, VacationTypeColor> recordInfoToColor) {

        final LocalDate today = LocalDate.now(clock);
        final List<WorkingTime> workingTimeList = workingTimeService.getByPersons(personList);
        final List<AbsencePeriod> openAbsences = absenceService.getOpenAbsences(personList, dateRange.startDate(), dateRange.endDate());

        final HashMap<Integer, AbsenceOverviewMonthDto> monthsByNr = new HashMap<>();

        final Map<Person, List<AbsencePeriod.Record>> absencePeriodRecordsByPerson = openAbsences.stream()
            .map(AbsencePeriod::absenceRecords)
            .flatMap(List::stream)
            .collect(groupingBy(AbsencePeriod.Record::getPerson));

        final Map<Person, Map<LocalDate, PublicHoliday>> publicHolidaysOfAllPersons = new HashMap<>();
        for (Person person : personList) {
            publicHolidaysOfAllPersons.put(person, getPublicHolidaysOfPerson(dateRange, person));
        }

        for (LocalDate date : dateRange) {
            final AbsenceOverviewMonthDto monthView = monthsByNr.computeIfAbsent(date.getMonthValue(),
                monthValue -> initializeAbsenceOverviewMonthDto(date, personList, locale));

            final AbsenceOverviewMonthDayDto tableHeadDay = tableHeadDay(date, today, locale);
            monthView.getDays().add(tableHeadDay);

            final Map<AbsenceOverviewMonthPersonDto, Person> personByView = personList.stream()
                .collect(
                    toMap(person -> monthView.getPersons().stream()
                        .filter(view -> view.getId().equals(person.getId()))
                        .findFirst()
                        .orElse(null), Function.identity())
                );

            // create an absence day dto for every person of the department
            for (AbsenceOverviewMonthPersonDto personView : monthView.getPersons()) {

                final Person person = personByView.get(personView);

                final List<WorkingTime> personWorkingTimeList = workingTimeList
                    .stream()
                    .filter(workingTime -> workingTime.getPerson().equals(person))
                    .sorted(comparing(WorkingTime::getValidFrom).reversed())
                    .toList();

                final List<AbsencePeriod.Record> personAbsenceRecordsForDate = Optional.ofNullable(absencePeriodRecordsByPerson.get(person))
                    .stream()
                    .flatMap(List::stream)
                    .filter(absenceRecord -> absenceRecord.getDate().isEqual(date))
                    .toList();

                final AbsenceOverviewDayType personViewDayType = Optional.ofNullable(publicHolidaysOfAllPersons.get(person).get(date))
                    .map(publicHoliday -> getAbsenceOverviewDayType(personAbsenceRecordsForDate, shouldAnonymizeAbsenceType, publicHoliday, recordInfoToColor))
                    .orElseGet(() -> getAbsenceOverviewDayType(personAbsenceRecordsForDate, shouldAnonymizeAbsenceType, recordInfoToColor))
                    .build();

                personView.getDays().add(new AbsenceOverviewPersonDayDto(personViewDayType, isWorkday(date, personWorkingTimeList)));
            }
        }

        return new ArrayList<>(monthsByNr.values());
    }

    private boolean isWorkday(LocalDate date, List<WorkingTime> workingTimeList) {
        return workingTimeList
            .stream()
            .filter(w -> w.getValidFrom().isBefore(date) || w.getValidFrom().isEqual(date))
            .findFirst()
            .map(w -> w.isWorkingDay(date.getDayOfWeek()))
            .orElse(false);
    }

    private Map<LocalDate, PublicHoliday> getPublicHolidaysOfPerson(DateRange dateRange, Person person) {
        return workingTimeService.getFederalStatesByPersonAndDateRange(person, dateRange)
            .entrySet().stream()
            .map(entry -> publicHolidaysService.getPublicHolidays(entry.getKey().startDate(), entry.getKey().endDate(), entry.getValue()))
            .flatMap(List::stream)
            .collect(toMap(PublicHoliday::date, Function.identity(), (publicHoliday, publicHoliday2) -> new PublicHoliday(publicHoliday.date(), publicHoliday.dayLength(), publicHoliday.description().concat("/").concat(publicHoliday2.description()))));

    }

    private AbsenceOverviewMonthDto initializeAbsenceOverviewMonthDto(LocalDate date, List<Person> personList, Locale locale) {

        final List<AbsenceOverviewMonthPersonDto> monthViewPersons = personList.stream()
            .map(AbsenceOverviewViewController::initializeAbsenceOverviewMonthPersonDto)
            .toList();

        return new AbsenceOverviewMonthDto(getMonthText(date, locale), new ArrayList<>(), monthViewPersons);
    }

    private static AbsenceOverviewMonthPersonDto initializeAbsenceOverviewMonthPersonDto(Person person) {

        final Long id = person.getId();
        final String firstName = person.getFirstName();
        final String lastName = person.getLastName();
        final String initials = person.getInitials();
        final String gravatarUrl = person.getGravatarURL();

        return new AbsenceOverviewMonthPersonDto(id, firstName, lastName, initials, gravatarUrl, new ArrayList<>());
    }

    private AbsenceOverviewDayType.Builder getAbsenceOverviewDayType(List<AbsencePeriod.Record> absenceRecords,
                                                                     Function<AbsencePeriod.RecordInfo, Boolean> shouldAnonymizeAbsenceType,
                                                                     PublicHoliday publicHoliday,
                                                                     Function<AbsencePeriod.RecordInfo, VacationTypeColor> recordInfoToColor) {

        AbsenceOverviewDayType.Builder builder = getAbsenceOverviewDayType(absenceRecords, shouldAnonymizeAbsenceType, recordInfoToColor);
        if (publicHoliday.dayLength().isMorning()) {
            builder = builder.publicHolidayMorning();
        }
        if (publicHoliday.dayLength().isNoon()) {
            builder = builder.publicHolidayNoon();
        }
        if (publicHoliday.dayLength().isFull()) {
            builder = builder.publicHolidayFull();
        }
        return builder;
    }

    private AbsenceOverviewDayType.Builder getAbsenceOverviewDayType(List<AbsencePeriod.Record> absenceRecords,
                                                                     Function<AbsencePeriod.RecordInfo, Boolean> shouldAnonymizeAbsenceType,
                                                                     Function<AbsencePeriod.RecordInfo, VacationTypeColor> recordInfoToColor) {
        if (absenceRecords.isEmpty()) {
            return AbsenceOverviewDayType.builder();
        }

        AbsenceOverviewDayType.Builder builder = AbsenceOverviewDayType.builder();
        for (AbsencePeriod.Record absenceRecord : absenceRecords) {
            if (absenceRecord.isHalfDayAbsence()) {
                builder = getAbsenceOverviewDayTypeForHalfDay(builder, absenceRecord, shouldAnonymizeAbsenceType, recordInfoToColor);
            } else {
                builder = getAbsenceOverviewDayTypeForFullDay(builder, absenceRecord, shouldAnonymizeAbsenceType, recordInfoToColor);
            }
        }

        return builder;
    }

    private AbsenceOverviewDayType.Builder getAbsenceOverviewDayTypeForHalfDay(AbsenceOverviewDayType.Builder builder,
                                                                               AbsencePeriod.Record absenceRecord,
                                                                               Function<AbsencePeriod.RecordInfo, Boolean> shouldAnonymizeAbsenceType,
                                                                               Function<AbsencePeriod.RecordInfo, VacationTypeColor> recordInfoToColor) {

        final Optional<AbsencePeriod.RecordInfo> morning = absenceRecord.getMorning();
        final Optional<AbsencePeriod.RecordInfo> noon = absenceRecord.getNoon();

        final AbsencePeriod.AbsenceType morningGenericAbsenceType = morning.map(AbsencePeriod.RecordInfo::getAbsenceType).orElse(null);
        final AbsencePeriod.AbsenceType noonGenericAbsenceType = noon.map(AbsencePeriod.RecordInfo::getAbsenceType).orElse(null);

        final boolean anonymizeMorning = morning.map(shouldAnonymizeAbsenceType).orElse(false);
        final boolean anonymizeNoon = noon.map(shouldAnonymizeAbsenceType).orElse(false);

        if (AbsencePeriod.AbsenceType.SICK.equals(morningGenericAbsenceType)) {
            if (anonymizeMorning) {
                return builder.colorMorning(ANONYMIZED_ABSENCE_COLOR).absenceMorning();
            } else {
                // sickNote has only one of two statuses: whether WAITING or ACTIVE
                final boolean hasStatusWaiting = morning.map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);
                return hasStatusWaiting ? builder.waitingSickNoteMorning() : builder.activeSickNoteMorning();
            }
        }
        if (AbsencePeriod.AbsenceType.SICK.equals(noonGenericAbsenceType)) {
            if (anonymizeNoon) {
                return builder.colorNoon(ANONYMIZED_ABSENCE_COLOR).absenceNoon();
            } else {
                // sickNote has only one of two statuses: whether WAITING or ACTIVE
                final boolean hasStatusWaiting = noon.map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);
                return hasStatusWaiting ? builder.waitingSickNoteNoon() : builder.activeSickNoteNoon();
            }
        }

        // public holiday and no_workday both don't need morning or noon colors
        boolean ignoreMorning = hasGenericAbsenceType(morning, AbsencePeriod.AbsenceType.NO_WORKDAY)
            || hasGenericAbsenceType(morning, AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);
        boolean ignoreNoon = hasGenericAbsenceType(noon, AbsencePeriod.AbsenceType.NO_WORKDAY)
            || hasGenericAbsenceType(noon, AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY);

        if (!ignoreMorning && morning.isPresent()) {
            if (anonymizeMorning) {
                builder.colorMorning(ANONYMIZED_ABSENCE_COLOR);
            } else {
                final VacationTypeColor color = recordInfoToColor.apply(morning.orElseThrow());
                builder.colorMorning(color);
            }
        }
        if (!ignoreNoon && noon.isPresent()) {
            if (anonymizeNoon) {
                builder.colorNoon(ANONYMIZED_ABSENCE_COLOR);
            } else {
                final VacationTypeColor color = recordInfoToColor.apply(noon.orElseThrow());
                builder.colorNoon(color);
            }
        }

        if (!ignoreMorning && morning.isPresent()) {
            final boolean morningWaiting = morning.map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);
            if (morningWaiting) {
                return anonymizeMorning ? builder.absenceMorning() : builder.waitingAbsenceMorning();
            }

            final boolean morningTemporaryAllowed = morning.map(AbsencePeriod.RecordInfo::hasStatusTemporaryAllowed).orElse(false);
            if (morningTemporaryAllowed) {
                return anonymizeMorning ? builder.absenceMorning() : builder.temporaryAllowedAbsenceMorning();
            }

            final boolean morningAllowed = morning.map(AbsencePeriod.RecordInfo::hasStatusAllowed).orElse(false);
            if (morningAllowed) {
                return builder.absenceMorning();
            }

            final boolean morningAllowedCancellationRequested = morning.map(AbsencePeriod.RecordInfo::hasStatusAllowedCancellationRequested).orElse(false);
            if (morningAllowedCancellationRequested) {
                return anonymizeMorning ? builder.absenceMorning() : builder.allowedCancellationRequestedAbsenceMorning();
            }
        }

        if (!ignoreNoon && noon.isPresent()) {
            final boolean noonWaiting = noon.map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);
            if (noonWaiting) {
                return anonymizeNoon ? builder.absenceNoon() : builder.waitingAbsenceNoon();
            }

            final boolean noonTemporaryAllowed = noon.map(AbsencePeriod.RecordInfo::hasStatusTemporaryAllowed).orElse(false);
            if (noonTemporaryAllowed) {
                return anonymizeNoon ? builder.absenceNoon() : builder.temporaryAllowedAbsenceNoon();
            }

            final boolean noonAllowedCancellationRequested = noon.map(AbsencePeriod.RecordInfo::hasStatusAllowedCancellationRequested).orElse(false);
            if (noonAllowedCancellationRequested) {
                return anonymizeNoon ? builder.absenceNoon() : builder.allowedCancellationRequestedAbsenceNoon();
            }

            return builder.absenceNoon();
        }

        return builder;
    }

    private boolean hasGenericAbsenceType(Optional<AbsencePeriod.RecordInfo> recordInfo, AbsencePeriod.AbsenceType genericAbsenceType) {
        return recordInfo.map(AbsencePeriod.RecordInfo::getAbsenceType).map(genericAbsenceType::equals).orElse(false);
    }

    private AbsenceOverviewDayType.Builder getAbsenceOverviewDayTypeForFullDay(AbsenceOverviewDayType.Builder builder,
                                                                               AbsencePeriod.Record absenceRecord,
                                                                               Function<AbsencePeriod.RecordInfo, Boolean> shouldAnonymizeAbsenceType,
                                                                               Function<AbsencePeriod.RecordInfo, VacationTypeColor> recordInfoToColor) {

        final Optional<AbsencePeriod.RecordInfo> morning = absenceRecord.getMorning();
        final Optional<AbsencePeriod.RecordInfo> noon = absenceRecord.getNoon();
        final Optional<AbsencePeriod.AbsenceType> morningType = morning.map(AbsencePeriod.RecordInfo::getAbsenceType);
        final Optional<AbsencePeriod.AbsenceType> noonType = noon.map(AbsencePeriod.RecordInfo::getAbsenceType);

        final boolean sickMorning = morningType.map(AbsencePeriod.AbsenceType.SICK::equals).orElse(false);
        final boolean sickNoon = noonType.map(AbsencePeriod.AbsenceType.SICK::equals).orElse(false);
        final boolean sickFull = sickMorning && sickNoon;

        // morning and noon should both exist, actually. otherwise this method is not called.
        final boolean anonymizeAbsenceType = morning.map(shouldAnonymizeAbsenceType)
            .orElseGet(() -> noon.map(shouldAnonymizeAbsenceType).orElse(false));

        if (sickFull) {
            if (anonymizeAbsenceType) {
                return builder.colorFull(ANONYMIZED_ABSENCE_COLOR).absenceFull();
            } else {
                // sickNote has only one of two statuses: whether WAITING or ACTIVE
                final boolean morningWaiting = morning.map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);
                if (morningWaiting) {
                    return builder.waitingSickNoteFull();
                }
                return builder.activeSickNoteFull();
            }
        }

        if (morningType.isPresent()) {
            if (morningType.get().equals(AbsencePeriod.AbsenceType.NO_WORKDAY)) {
                // no workday is highlighted differently as an absenceFull day. no color required to be set.
                return builder;
            }
            if (morningType.get().equals(AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY)) {
                // public holiday is not required to be handled here currently.
                // however, this has to be handled as soon as a person could work despite public holiday.
                return builder;
            }
        }

        final boolean morningWaiting = morning.map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);
        final boolean noonWaiting = noon.map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);

        final boolean morningTemporaryAllowed = morning.map(AbsencePeriod.RecordInfo::hasStatusTemporaryAllowed).orElse(false);
        final boolean noonTemporaryAllowed = noon.map(AbsencePeriod.RecordInfo::hasStatusTemporaryAllowed).orElse(false);

        final boolean morningAllowedCancellationRequested = morning.map(AbsencePeriod.RecordInfo::hasStatusAllowedCancellationRequested).orElse(false);
        final boolean noonAllowedCancellationRequested = noon.map(AbsencePeriod.RecordInfo::hasStatusAllowedCancellationRequested).orElse(false);

        if (anonymizeAbsenceType) {
            builder.colorFull(ANONYMIZED_ABSENCE_COLOR);
        } else {
            // full day absence consists of a morning and a noon recordInfo.
            // both recordInfos have the same vacationType, therefore we can use the morning color.
            final VacationTypeColor color = recordInfoToColor.apply(morning.orElseThrow());
            builder.colorFull(color);
        }

        if (morningWaiting && noonWaiting) {
            return anonymizeAbsenceType ? builder.absenceFull() : builder.waitingAbsenceFull();
        } else if (morningTemporaryAllowed && noonTemporaryAllowed) {
            return anonymizeAbsenceType ? builder.absenceFull() : builder.temporaryAllowedAbsenceFull();
        } else if (morningAllowedCancellationRequested && noonAllowedCancellationRequested) {
            return anonymizeAbsenceType ? builder.absenceFull() : builder.allowedCancellationRequestedAbsenceFull();
        } else if (!morningWaiting && !noonWaiting) {
            return builder.absenceFull();
        }

        return builder;
    }

    private VacationTypeColor recordInfoToColor(AbsencePeriod.RecordInfo recordInfo, Function<Long, VacationType<?>> vacationTypById) {
        return recordInfo.getTypeId()
            .map(vacationTypById)
            .map(VacationType::getColor)
            // sick-note does not have a vacationTypeId, but is handled separately. therefore just throw.
            // same for public-holiday and no-workday.
            .orElseThrow();
    }

    private String getSelectedMonth(String month, LocalDate startDate) {
        String selectedMonth = "";
        if (month == null) {
            selectedMonth = String.valueOf(startDate.getMonthValue());
        } else if (hasText(month)) {
            selectedMonth = month;
        }
        return selectedMonth;
    }

    private AbsenceOverviewMonthDayDto tableHeadDay(LocalDate date, LocalDate today, Locale locale) {

        final String tableHeadDayText = String.format("%02d", date.getDayOfMonth());
        final String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, locale);
        final boolean isToday = date.isEqual(today);

        return new AbsenceOverviewMonthDayDto(null, tableHeadDayText, dayOfWeek, isWeekend(date), isToday);
    }

    private String getMonthText(LocalDate date, Locale locale) {
        return messageSource.getMessage(getMonthMessageCode(date), new Object[]{}, locale);
    }

    private String getMonthMessageCode(LocalDate localDate) {
        return switch (localDate.getMonthValue()) {
            case 1 -> "month.january";
            case 2 -> "month.february";
            case 3 -> "month.march";
            case 4 -> "month.april";
            case 5 -> "month.may";
            case 6 -> "month.june";
            case 7 -> "month.july";
            case 8 -> "month.august";
            case 9 -> "month.september";
            case 10 -> "month.october";
            case 11 -> "month.november";
            case 12 -> "month.december";
            default ->
                throw new IllegalStateException("month value not in range of 1 to 12 cannot be mapped to a message key.");
        };
    }

    private LocalDate getStartDate(Integer year, String month) {
        return getStartOrEndDate(year, month, TemporalAdjusters::firstDayOfYear, TemporalAdjusters::firstDayOfMonth);
    }

    private LocalDate getEndDate(Integer year, String month) {
        return getStartOrEndDate(year, month, TemporalAdjusters::lastDayOfYear, TemporalAdjusters::lastDayOfMonth);
    }

    private LocalDate getStartOrEndDate(Integer year, String month, Supplier<TemporalAdjuster> firstOrLastOfYearSupplier,
                                        Supplier<TemporalAdjuster> firstOrLastOfMonthSupplier) {

        final LocalDate now = LocalDate.now(clock);

        if (year != null) {
            if (hasText(month)) {
                return now.withYear(year).withMonth(parseInt(month)).with(firstOrLastOfMonthSupplier.get());
            }
            if ("".equals(month)) {
                return now.withYear(year).with(firstOrLastOfYearSupplier.get());
            }
            return now.withYear(year).with(firstOrLastOfMonthSupplier.get());
        }

        if (hasText(month)) {
            return now.withMonth(parseInt(month)).with(firstOrLastOfMonthSupplier.get());
        }
        return now.with(firstOrLastOfMonthSupplier.get());
    }

    private static boolean isWeekend(LocalDate date) {
        final DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == SATURDAY || dayOfWeek == SUNDAY;
    }

    private List<Person> getActiveMembersOfPerson(final Person person) {

        if (person.hasRole(BOSS) || person.hasRole(OFFICE)) {
            return personService.getActivePersons();
        }

        final List<Person> relevantPersons = new ArrayList<>();
        if (person.hasRole(DEPARTMENT_HEAD)) {
            departmentService.getMembersForDepartmentHead(person).stream()
                .filter(member -> !member.hasRole(INACTIVE))
                .collect(toCollection(() -> relevantPersons));
        }

        if (person.hasRole(SECOND_STAGE_AUTHORITY)) {
            departmentService.getMembersForSecondStageAuthority(person).stream()
                .filter(member -> !member.hasRole(INACTIVE))
                .collect(toCollection(() -> relevantPersons));
        }

        return relevantPersons.stream()
            .distinct()
            .toList();
    }

    private static VacationTypeColorDto toVacationTypeColorsDto(VacationType<?> vacationType, Locale locale) {
        return new VacationTypeColorDto(vacationType.getLabel(locale), vacationType.getColor());
    }

    private VacationTypeColorDto getAnonymizedAbsenceTypeColor(Locale locale) {
        final String label = messageSource.getMessage("absences.overview.absence", new Object[]{}, locale);
        return new VacationTypeColorDto(label, ANONYMIZED_ABSENCE_COLOR);
    }
}
