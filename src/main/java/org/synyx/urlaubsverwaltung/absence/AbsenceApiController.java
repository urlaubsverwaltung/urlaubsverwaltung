package org.synyx.urlaubsverwaltung.absence;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.absence.AbsenceDto.AbsenceType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.absence.AbsenceDto.AbsenceType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.absence.AbsenceDto.AbsenceType.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.absence.AbsenceDto.AbsenceType.VACATION;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@RestControllerAdviceMarker
@Tag(name = "absences", description = "Absences: Get all absences for a certain period")
@RestController
@RequestMapping("/api/persons/{personId}")
public class AbsenceApiController {

    public static final String ABSENCES = "absences";

    private final PersonService personService;
    private final AbsenceService absenceService;

    @Autowired
    public AbsenceApiController(PersonService personService, AbsenceService absenceService) {
        this.personService = personService;
        this.absenceService = absenceService;
    }

    @Operation(
        summary = "Get all absences for a certain period and person",
        description = "Get all absences for a certain period and person"
    )
    @GetMapping(ABSENCES)
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public AbsencesDto personsAbsences(
        @Parameter(description = "ID of the person")
        @PathVariable("personId")
        Long personId,
        @Parameter(description = "start of interval to get absences from (inclusive)")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "end of interval to get absences from (inclusive)")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate endDate,
        @Parameter(description = "Type of absences like vacation, sick_note, public_holiday and no_workday")
        @RequestParam(value = "absence-types", required = false, defaultValue = "vacation, sick_note, public_holiday, no_workday")
        List<String> absenceTypes) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Start date " + startDate + " must not be after end date " + endDate);
        }

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for ID=" + personId);
        }

        final List<AbsenceDto> absences = getAbsences(startDate, endDate, optionalPerson.get(), toAbsenceTypes(absenceTypes));
        return new AbsencesDto(absences);
    }

    private List<AbsenceDto> getAbsences(LocalDate start, LocalDate end, Person person, List<AbsenceDto.AbsenceType> types) {

        final Predicate<AbsenceDto> vacationAsked = dto -> types.contains(VACATION);
        final Predicate<AbsenceDto> sickAsked = dto -> types.contains(SICK_NOTE);
        final Predicate<AbsenceDto> noWorkDayAsked = dto -> types.contains(NO_WORKDAY);
        final Predicate<AbsenceDto> publicHolidayAsked = dto -> types.contains(PUBLIC_HOLIDAY);

        final Predicate<AbsenceDto> isVacation = dto -> dto.getAbsenceType().equals(VACATION);
        final Predicate<AbsenceDto> isSick = dto -> dto.getAbsenceType().equals(SICK_NOTE);
        final Predicate<AbsenceDto> isNoWorkDay = dto -> dto.getAbsenceType().equals(NO_WORKDAY);
        final Predicate<AbsenceDto> isPublicHoliday = dto -> dto.getAbsenceType().equals(PUBLIC_HOLIDAY);

        return absenceService.getOpenAbsences(person, start, end)
            .stream()
            .flatMap(this::toAbsenceDto)
            .filter(
                vacationAsked.and(isVacation)
                    .or(sickAsked.and(isSick))
                    .or(noWorkDayAsked.and(isNoWorkDay))
                    .or(publicHolidayAsked.and(isPublicHoliday))
            )
            .toList();
    }

    private Stream<AbsenceDto> toAbsenceDto(AbsencePeriod absence) {
        return absence.getAbsenceRecords().stream()
            .map(this::toAbsenceDto)
            .flatMap(List::stream);
    }

    private List<AbsenceDto> toAbsenceDto(AbsencePeriod.Record absenceRecord) {

        final LocalDate date = absenceRecord.getDate();

        final Optional<AbsencePeriod.RecordInfo> maybeMorning = absenceRecord.getMorning();
        final Optional<AbsencePeriod.AbsenceType> maybeMorningType = maybeMorning.map(AbsencePeriod.RecordInfo::getAbsenceType);

        final Optional<AbsencePeriod.RecordInfo> maybeNoon = absenceRecord.getNoon();
        final Optional<AbsencePeriod.AbsenceType> maybeNoonType = maybeNoon.map(AbsencePeriod.RecordInfo::getAbsenceType);

        if (maybeMorningType.isPresent() && maybeNoonType.isPresent()) {
            if (maybeMorningType.equals(maybeNoonType)) {
                return List.of(toAbsenceDto(date, DayLength.FULL, maybeMorning.orElseThrow()));
            } else {
                return List.of(toAbsenceDto(date, DayLength.MORNING, maybeMorning.orElseThrow()), toAbsenceDto(date, DayLength.NOON, maybeNoon.orElseThrow()));
            }
        } else if (maybeMorningType.isPresent()) {
            return List.of(toAbsenceDto(date, DayLength.MORNING, maybeMorning.orElseThrow()));
        } else if (maybeNoonType.isPresent()) {
            return List.of(toAbsenceDto(date, DayLength.NOON, maybeNoon.orElseThrow()));
        }

        return List.of();
    }

    private AbsenceDto toAbsenceDto(LocalDate date, DayLength dayLength, AbsencePeriod.RecordInfo recordInfo) {
        final AbsenceDto.AbsenceType type = toAbsenceTypes(recordInfo.getAbsenceType());
        final String status = recordInfo.getStatus().name();
        return new AbsenceDto(date, type, recordInfo.getId().orElse(null), status, dayLength, recordInfo.getTypeCategory().orElse(null), recordInfo.getTypeId().orElse(null));
    }

    private List<AbsenceDto.AbsenceType> toAbsenceTypes(List<String> dayAbsenceTypes) {
        if (dayAbsenceTypes.isEmpty()) {
            return List.of();
        }

        try {
            return dayAbsenceTypes.stream()
                .map(String::toUpperCase)
                .map(AbsenceDto.AbsenceType::valueOf)
                .toList();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }
    }

    private AbsenceDto.AbsenceType toAbsenceTypes(AbsencePeriod.AbsenceType genericAbsenceType) {
        return switch (genericAbsenceType) {
            case VACATION -> VACATION;
            case SICK -> SICK_NOTE;
            case NO_WORKDAY -> NO_WORKDAY;
            case PUBLIC_HOLIDAY -> PUBLIC_HOLIDAY;
        };
    }
}
