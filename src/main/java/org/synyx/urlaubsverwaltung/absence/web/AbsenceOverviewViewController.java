package org.synyx.urlaubsverwaltung.absence.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
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
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNotePermissionEvaluator;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isWeekend;

@RequestMapping("/web/absences")
@Controller
public class AbsenceOverviewViewController implements HasLaunchpad, HasPersonSearch {

    private static final VacationTypeColor ANONYMIZED_ABSENCE_COLOR = VacationTypeColor.YELLOW;
    // a bar may continue beyond the requested range - the margin tells whether it does
    static final int BAR_TIMELINE_MARGIN_DAYS = 14;
    private static final String SICK_NOTE_BAR_COLOR = "SICK_NOTE";

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final PublicHolidaysService publicHolidaysService;
    private final AbsenceService absenceService;
    private final WorkingTimeService workingTimeService;
    private final VacationTypeService vacationTypeService;
    private final SickNotePermissionEvaluator sickNotePermissionEvaluator;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final MessageSource messageSource;
    private final Clock clock;

    AbsenceOverviewViewController(
        PersonService personService, DepartmentService departmentService,
        PublicHolidaysService publicHolidaysService, AbsenceService absenceService,
        WorkingTimeService workingTimeService, VacationTypeService vacationTypeService,
        SickNotePermissionEvaluator sickNotePermissionEvaluator,
        PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy, PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier,
        MessageSource messageSource, Clock clock
    ) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.publicHolidaysService = publicHolidaysService;
        this.absenceService = absenceService;
        this.workingTimeService = workingTimeService;
        this.vacationTypeService = vacationTypeService;
        this.sickNotePermissionEvaluator = sickNotePermissionEvaluator;
        this.defaultPersonSuggestionUrlStrategy = defaultPersonSuggestionUrlStrategy;
        this.personSearchUiFragmentSupplier = personSearchUiFragmentSupplier;
        this.messageSource = messageSource;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(List.class, new CustomCollectionEditor(List.class));
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return defaultPersonSuggestionUrlStrategy;
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchUiFragmentSupplier;
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
                final List<String> selectedDepartmentNames = getSelectedDepartmentNames(rawSelectedDepartments, visibleDepartments, signedInUser);
                model.addAttribute("selectedDepartments", selectedDepartmentNames);

                overviewPersons = visibleDepartments.stream()
                    .filter(department -> selectedDepartmentNames.contains(department.getName()))
                    .map(Department::getMembers)
                    .flatMap(List::stream)
                    .filter(Person::isActive)
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

        final boolean isSignedInUserBossOrOffice = signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE);
        final boolean isSignedInUserAllowedToViewAllSickNotes = sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(signedInUser);
        // the managed members only matter for sick notes when the signed-in user may not see all of them anyway
        final List<Person> managedMembersOfSignedInUser = isSignedInUserBossOrOffice && isSignedInUserAllowedToViewAllSickNotes
            ? List.of()
            : getActiveManagedMembersOfPerson(signedInUser);
        final List<Person> membersOfSignedInUser = isSignedInUserBossOrOffice ? personService.getActivePersons() : managedMembersOfSignedInUser;
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

        final Function<AbsencePeriod.RecordInfo, String> detailUrl = recordInfo -> absenceDetailUrl(recordInfo, signedInUser,
            membersOfSignedInUser, managedMembersOfSignedInUser, isSignedInUserAllowedToViewAllSickNotes);

        final DateRange dateRange = new DateRange(startDate, endDate);
        final List<AbsenceOverviewMonthDto> months = getAbsenceOverViewMonthModels(dateRange, overviewPersons, locale, shouldAnonymizeAbsenceType, recordInfoToColor, vacationTypesById::get, detailUrl);
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

    private List<String> getSelectedDepartmentNames(List<String> rawSelectedDepartments, List<Department> departments, Person signedInUser) {
        final List<String> preparedSelectedDepartments = rawSelectedDepartments.stream().filter(StringUtils::hasText).toList();
        if (!preparedSelectedDepartments.isEmpty()) {
            return preparedSelectedDepartments;
        }

        final List<String> departmentNamesOfSignedInUser = departmentService.getAssignedDepartmentsOfMember(signedInUser).stream()
            .map(Department::getName)
            .toList();
        return departmentNamesOfSignedInUser.isEmpty() ? List.of(departments.getFirst().getName()) : departmentNamesOfSignedInUser;
    }

    private List<AbsenceOverviewMonthDto> getAbsenceOverViewMonthModels(DateRange dateRange,
                                                                        List<Person> personList,
                                                                        Locale locale,
                                                                        Function<AbsencePeriod.RecordInfo, Boolean> shouldAnonymizeAbsenceType,
                                                                        Function<AbsencePeriod.RecordInfo, VacationTypeColor> recordInfoToColor,
                                                                        Function<Long, VacationType<?>> vacationTypeById,
                                                                        Function<AbsencePeriod.RecordInfo, String> detailUrl) {

        final LocalDate today = LocalDate.now(clock);
        final DateRange barTimeline = new DateRange(dateRange.startDate().minusDays(BAR_TIMELINE_MARGIN_DAYS), dateRange.endDate().plusDays(BAR_TIMELINE_MARGIN_DAYS));
        final List<AbsencePeriod> openAbsences = absenceService.getOpenAbsences(personList, barTimeline.startDate(), barTimeline.endDate());

        // index the records once - a lookup per person and date must not scan all records of the person
        final Map<Person, Map<LocalDate, List<AbsencePeriod.Record>>> absenceRecordsByPersonAndDate = openAbsences.stream()
            .map(AbsencePeriod::absenceRecords)
            .flatMap(List::stream)
            .collect(groupingBy(AbsencePeriod.Record::getPerson, groupingBy(AbsencePeriod.Record::getDate)));

        // load the working times of all persons with a single query instead of one query per person
        final Map<Person, Map<DateRange, WorkingTime>> workingTimesByPerson = workingTimeService.getWorkingTimesByPersonsAndDateRange(personList, dateRange);

        final Map<Person, Map<LocalDate, PublicHoliday>> publicHolidaysOfAllPersons = new HashMap<>();
        for (Person person : personList) {
            publicHolidaysOfAllPersons.put(person, getPublicHolidaysOfPerson(workingTimesByPerson.getOrDefault(person, Map.of())));
        }

        // resolve each message at most once per request
        final Map<String, String> messages = new HashMap<>();
        final UnaryOperator<String> message = code -> messages.computeIfAbsent(code, c -> messageSource.getMessage(c, new Object[]{}, locale));

        final Map<String, AbsenceBars.Absence> barAbsencesByKey = new HashMap<>();
        final Function<AbsencePeriod.RecordInfo, AbsenceBars.Absence> toBarAbsence = recordInfo -> {
            final boolean anonymize = shouldAnonymizeAbsenceType.apply(recordInfo);
            return barAbsencesByKey.computeIfAbsent(barAbsenceKey(recordInfo, anonymize),
                key -> barAbsence(key, recordInfo, anonymize, vacationTypeById, detailUrl, locale, message));
        };

        final Map<Person, Map<LocalDate, List<AbsenceBars.Piece>>> barPiecesByPerson = new HashMap<>();
        for (Person person : personList) {
            final Map<LocalDate, PublicHoliday> publicHolidays = publicHolidaysOfAllPersons.get(person);
            final Map<LocalDate, AbsenceBars.Day> barDays = new HashMap<>();
            absenceRecordsByPersonAndDate.getOrDefault(person, Map.of()).forEach((date, records) -> {
                final Function<AbsencePeriod.AbsenceType, String> gapTitle = gapType -> gapType == AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY
                    ? Optional.ofNullable(publicHolidays.get(date)).map(PublicHoliday::description).orElseGet(() -> message.apply("absences.overview.public-holiday"))
                    : message.apply("absences.overview.no-workday");
                barDays.put(date, new AbsenceBars.Day(
                    barHalfDay(records, AbsencePeriod.Record::getMorning, toBarAbsence, gapTitle),
                    barHalfDay(records, AbsencePeriod.Record::getNoon, toBarAbsence, gapTitle)
                ));
            });
            barPiecesByPerson.put(person, AbsenceBars.compute(barTimeline, barDays, dateRange));
        }

        final Map<Integer, AbsenceOverviewMonthDto> monthsByNr = new LinkedHashMap<>();
        // the first linked piece of a bar in a month - person, month and bar
        final Set<String> barsWithTabStop = new HashSet<>();

        for (LocalDate date : dateRange) {
            final AbsenceOverviewMonthDto monthView = monthsByNr.computeIfAbsent(date.getMonthValue(),
                _ -> initializeAbsenceOverviewMonthDto(date, personList, locale));

            final AbsenceOverviewMonthDayDto tableHeadDay = tableHeadDay(date, today, locale);
            monthView.getDays().add(tableHeadDay);

            // the person views of a month are created in the order of personList
            final List<AbsenceOverviewMonthPersonDto> personViews = monthView.getPersons();
            for (int index = 0; index < personList.size(); index++) {

                final Person person = personList.get(index);
                final Map<DateRange, WorkingTime> personWorkingTimes = workingTimesByPerson.getOrDefault(person, Map.of());

                final List<AbsencePeriod.Record> personAbsenceRecordsForDate = absenceRecordsByPersonAndDate
                    .getOrDefault(person, Map.of())
                    .getOrDefault(date, List.of());

                final AbsenceOverviewDayType personViewDayType = Optional.ofNullable(publicHolidaysOfAllPersons.get(person).get(date))
                    .map(publicHoliday -> getAbsenceOverviewDayType(personAbsenceRecordsForDate, shouldAnonymizeAbsenceType, publicHoliday, recordInfoToColor))
                    .orElseGet(() -> getAbsenceOverviewDayType(personAbsenceRecordsForDate, shouldAnonymizeAbsenceType, recordInfoToColor))
                    .build();

                final List<AbsenceOverviewBarPieceDto> bars = new ArrayList<>();
                for (AbsenceBars.Piece piece : barPiecesByPerson.get(person).getOrDefault(date, List.of())) {
                    final boolean tabStop = piece.absence().detailUrl() != null
                        && barsWithTabStop.add(person.getId() + "/" + date.getMonthValue() + "/" + piece.absence().key());
                    bars.add(toBarPieceDto(piece, tabStop, message));
                }
                final String publicHolidayName = Optional.ofNullable(publicHolidaysOfAllPersons.get(person).get(date))
                    .map(PublicHoliday::description)
                    .orElse(null);

                personViews.get(index).getDays().add(new AbsenceOverviewPersonDayDto(personViewDayType, isWorkday(date, personWorkingTimes), bars, publicHolidayName));
            }
        }

        return new ArrayList<>(monthsByNr.values());
    }

    private boolean isWorkday(LocalDate date, Map<DateRange, WorkingTime> workingTimesByDateRange) {
        return workingTimesByDateRange.entrySet().stream()
            .filter(entry -> entry.getKey().isOverlapping(new DateRange(date, date)))
            .findFirst()
            .map(entry -> entry.getValue().isWorkingDay(date.getDayOfWeek()))
            .orElse(false);
    }

    private Map<LocalDate, PublicHoliday> getPublicHolidaysOfPerson(Map<DateRange, WorkingTime> workingTimesByDateRange) {
        return workingTimesByDateRange
            .entrySet().stream()
            .map(entry -> publicHolidaysService.getPublicHolidays(entry.getKey().startDate(), entry.getKey().endDate(), entry.getValue().getFederalState()))
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

    private static AbsenceBars.HalfDay barHalfDay(List<AbsencePeriod.Record> records,
                                                  Function<AbsencePeriod.Record, Optional<AbsencePeriod.RecordInfo>> half,
                                                  Function<AbsencePeriod.RecordInfo, AbsenceBars.Absence> toBarAbsence,
                                                  Function<AbsencePeriod.AbsenceType, String> gapTitle) {

        final List<AbsencePeriod.RecordInfo> recordInfos = records.stream().map(half).flatMap(Optional::stream).toList();

        // a sick note during a vacation is what the person really is - it wins over the vacation
        final AbsenceBars.Absence absence = firstOfType(recordInfos, AbsencePeriod.AbsenceType.SICK)
            .or(() -> firstOfType(recordInfos, AbsencePeriod.AbsenceType.VACATION))
            .map(toBarAbsence)
            .orElse(null);

        // the name of a public holiday tells more than "no workday"
        final AbsenceBars.Gap gap = firstOfType(recordInfos, AbsencePeriod.AbsenceType.PUBLIC_HOLIDAY)
            .or(() -> firstOfType(recordInfos, AbsencePeriod.AbsenceType.NO_WORKDAY))
            .map(recordInfo -> new AbsenceBars.Gap(gapTitle.apply(recordInfo.getAbsenceType())))
            .orElse(null);

        return new AbsenceBars.HalfDay(absence, gap);
    }

    private static Optional<AbsencePeriod.RecordInfo> firstOfType(List<AbsencePeriod.RecordInfo> recordInfos, AbsencePeriod.AbsenceType absenceType) {
        return recordInfos.stream().filter(recordInfo -> recordInfo.getAbsenceType() == absenceType).findFirst();
    }

    private static String barAbsenceKey(AbsencePeriod.RecordInfo recordInfo, boolean anonymize) {
        // anonymized absences of a person are one bar - their boundaries would reveal what is hidden, e.g. a sick day
        if (anonymize) {
            return "ANONYMIZED-" + recordInfo.getPerson().getId();
        }
        return recordInfo.getAbsenceType().name() + "-" + recordInfo.getId().orElseThrow();
    }

    private static AbsenceBars.Absence barAbsence(String key, AbsencePeriod.RecordInfo recordInfo, boolean anonymize,
                                                  Function<Long, VacationType<?>> vacationTypeById,
                                                  Function<AbsencePeriod.RecordInfo, String> detailUrl, Locale locale,
                                                  UnaryOperator<String> message) {
        if (anonymize) {
            return new AbsenceBars.Absence(key, AbsenceBars.Status.ALLOWED, ANONYMIZED_ABSENCE_COLOR.name(), message.apply("absences.overview.absence"), null, true, null);
        }

        if (recordInfo.getAbsenceType() == AbsencePeriod.AbsenceType.SICK) {
            // sickNote has only one of two statuses: whether WAITING or ACTIVE
            final boolean waiting = recordInfo.hasStatusWaiting();
            return new AbsenceBars.Absence(key, waiting ? AbsenceBars.Status.WAITING : AbsenceBars.Status.ALLOWED, SICK_NOTE_BAR_COLOR,
                message.apply("absences.overview.sick"), waiting ? message.apply("sicknote.status.SUBMITTED") : null, false, detailUrl.apply(recordInfo));
        }

        final AbsenceBars.Status status = barStatus(recordInfo);
        final VacationType<?> vacationType = recordInfo.getTypeId().map(vacationTypeById).orElseThrow();
        return new AbsenceBars.Absence(key, status, vacationType.getColor().name(), vacationType.getLabel(locale), barStatusText(status, message), false, detailUrl.apply(recordInfo));
    }

    /**
     * Path of the page showing the absence, if the signed-in user may open it. Follows the rules of the pages
     * themselves: {@code DepartmentService#isSignedInUserAllowedToAccessPersonData} for applications for leave and
     * {@code SickNotePermissions#isAllowedToView} for sick notes. Seeing the vacation type is not enough - types that
     * are visible to everyone are shown to every colleague.
     *
     * @return the path, {@code null} if the signed-in user may not open the absence
     */
    private static String absenceDetailUrl(AbsencePeriod.RecordInfo recordInfo, Person signedInUser, List<Person> membersOfSignedInUser,
                                           List<Person> managedMembersOfSignedInUser, boolean isSignedInUserAllowedToViewAllSickNotes) {

        final Person person = recordInfo.getPerson();
        final boolean ownAbsence = person.equals(signedInUser);
        final Long id = recordInfo.getId().orElseThrow();

        return switch (recordInfo.getAbsenceType()) {
            case VACATION -> ownAbsence || membersOfSignedInUser.contains(person) ? "/web/application/" + id : null;
            case SICK -> ownAbsence || isSignedInUserAllowedToViewAllSickNotes || managedMembersOfSignedInUser.contains(person)
                ? "/web/sicknote/" + id
                : null;
            case PUBLIC_HOLIDAY, NO_WORKDAY -> null;
        };
    }

    private static AbsenceBars.Status barStatus(AbsencePeriod.RecordInfo recordInfo) {
        if (recordInfo.hasStatusWaiting()) {
            return AbsenceBars.Status.WAITING;
        }
        if (recordInfo.hasStatusTemporaryAllowed()) {
            return AbsenceBars.Status.TEMPORARY_ALLOWED;
        }
        if (recordInfo.hasStatusAllowedCancellationRequested()) {
            return AbsenceBars.Status.CANCELLATION_REQUESTED;
        }
        return AbsenceBars.Status.ALLOWED;
    }

    private static String barStatusText(AbsenceBars.Status status, UnaryOperator<String> message) {
        return switch (status) {
            case ALLOWED -> null;
            case WAITING -> message.apply("WAITING");
            case TEMPORARY_ALLOWED -> message.apply("TEMPORARY_ALLOWED");
            case CANCELLATION_REQUESTED -> message.apply("ALLOWED_CANCELLATION_REQUESTED");
        };
    }

    private static AbsenceOverviewBarPieceDto toBarPieceDto(AbsenceBars.Piece piece, boolean tabStop, UnaryOperator<String> message) {

        final AbsenceBars.Absence absence = piece.absence();

        final String title = switch (piece.kind()) {
            case SOLID -> barAbsenceTitle(absence, piece.half(), message);
            // anonymized, a sick note's own weekend must look like the weekend inside a vacation
            case BRIDGE -> piece.coveredByAbsence() && !absence.anonymized()
                ? barAbsenceTitle(absence, piece.half(), message) + ", " + piece.gap().title()
                : piece.gap().title();
        };

        final String label = piece.labelHalves() > 0 ? absence.label() : null;

        return new AbsenceOverviewBarPieceDto(piece.half(), piece.kind(), piece.roundedStart(), piece.roundedEnd(),
            absence.status(), absence.color(), label, piece.labelHalves(), title, absence.detailUrl(), tabStop);
    }

    private static String barAbsenceTitle(AbsenceBars.Absence absence, AbsenceBars.Half half, UnaryOperator<String> message) {
        if (absence.anonymized()) {
            return absence.label();
        }

        final List<String> parts = new ArrayList<>();
        parts.add(absence.label());
        if (half == AbsenceBars.Half.MORNING) {
            parts.add(message.apply("MORNING"));
        } else if (half == AbsenceBars.Half.NOON) {
            parts.add(message.apply("NOON"));
        }
        if (absence.statusText() != null) {
            parts.add(absence.statusText());
        }
        return String.join(", ", parts);
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

        final String tableHeadDayText = "%02d".formatted(date.getDayOfMonth());
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

    private List<Person> getActiveManagedMembersOfPerson(final Person person) {

        final List<Person> relevantPersons = new ArrayList<>();
        if (person.hasRole(DEPARTMENT_HEAD)) {
            departmentService.getMembersForDepartmentHead(person).stream()
                .filter(Person::isActive)
                .collect(toCollection(() -> relevantPersons));
        }

        if (person.hasRole(SECOND_STAGE_AUTHORITY)) {
            departmentService.getMembersForSecondStageAuthority(person).stream()
                .filter(Person::isActive)
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
