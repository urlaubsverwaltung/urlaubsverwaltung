package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentDeletedEvent;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;

/**
 * Implementation for {@link BlackoutPeriodService}.
 */
@Service
class BlackoutPeriodServiceImpl implements BlackoutPeriodService {

    private final BlackoutPeriodRepository blackoutPeriodRepository;
    private final DepartmentService departmentService;
    private final VacationTypeService vacationTypeService;
    private final PersonService personService;
    private final ApplicationService applicationService;
    private final Clock clock;

    BlackoutPeriodServiceImpl(
        BlackoutPeriodRepository blackoutPeriodRepository,
        DepartmentService departmentService,
        VacationTypeService vacationTypeService,
        PersonService personService,
        ApplicationService applicationService,
        Clock clock
    ) {
        this.blackoutPeriodRepository = blackoutPeriodRepository;
        this.departmentService = departmentService;
        this.vacationTypeService = vacationTypeService;
        this.personService = personService;
        this.applicationService = applicationService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlackoutPeriod> getAllBlackoutPeriods() {

        final List<BlackoutPeriodEntity> entities = blackoutPeriodRepository.findAll();
        if (entities.isEmpty()) {
            return List.of();
        }

        final Map<Long, Department> departmentsById = allDepartmentsById();
        final Map<Long, VacationType<?>> vacationTypesById = allVacationTypesById();

        return entities.stream()
            .map(entity -> mapToBlackoutPeriod(entity, departmentsById, vacationTypesById))
            .sorted(comparing(BlackoutPeriod::getStartDate))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlackoutPeriod> getBlackoutPeriodById(Long id) {
        return blackoutPeriodRepository.findById(id)
            .map(entity -> mapToBlackoutPeriod(entity, allDepartmentsById(), allVacationTypesById()));
    }

    @Override
    @Transactional
    public BlackoutPeriod create(BlackoutPeriod blackoutPeriod) {

        final BlackoutPeriodEntity entity = mapToBlackoutPeriodEntity(blackoutPeriod);
        entity.setId(null);
        entity.setCreatedAt(LocalDate.now(clock));
        entity.setLastModification(LocalDate.now(clock));

        final BlackoutPeriodEntity savedEntity = blackoutPeriodRepository.save(entity);

        return mapToBlackoutPeriod(savedEntity, allDepartmentsById(), allVacationTypesById());
    }

    @Override
    @Transactional
    public BlackoutPeriod update(BlackoutPeriod blackoutPeriod) {

        final BlackoutPeriodEntity existingEntity = blackoutPeriodRepository.findById(blackoutPeriod.getId())
            .orElseThrow(() -> new IllegalStateException("cannot update blackout period since it does not exist."));

        final BlackoutPeriodEntity entity = mapToBlackoutPeriodEntity(blackoutPeriod);
        entity.setCreatedAt(existingEntity.getCreatedAt());
        entity.setLastModification(LocalDate.now(clock));

        final BlackoutPeriodEntity savedEntity = blackoutPeriodRepository.save(entity);

        return mapToBlackoutPeriod(savedEntity, allDepartmentsById(), allVacationTypesById());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        blackoutPeriodRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlackoutPeriod> findBlockingBlackoutPeriod(Person person, LocalDate startDate, LocalDate endDate, VacationType<?> vacationType) {

        final List<BlackoutPeriodEntity> candidates = blackoutPeriodRepository.findOverlapping(startDate, endDate).stream()
            .filter(entity -> entity.isAllVacationTypes() || entity.getVacationTypeIds().contains(vacationType.getId()))
            .toList();

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        final Set<Long> personDepartmentIds = anyDepartmentScoped(candidates)
            ? departmentService.getDepartmentIdsByMembers(List.of(person)).getOrDefault(person.getIdAsPersonId(), Set.of())
            : Set.of();

        return candidates.stream()
            .filter(entity -> appliesToAnyOf(entity, personDepartmentIds))
            .min(comparing(BlackoutPeriodEntity::getStartDate))
            .map(entity -> mapToBlackoutPeriodWithoutDepartments(entity, vacationTypesByIdOf(List.of(entity))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlackoutPeriod> findBlackoutPeriodsForPerson(Person person, LocalDate startDate, LocalDate endDate) {
        return findBlackoutPeriodsForPersons(List.of(person), startDate, endDate)
            .getOrDefault(person.getIdAsPersonId(), List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<PersonId, List<BlackoutPeriod>> findBlackoutPeriodsForPersons(List<Person> persons, LocalDate startDate, LocalDate endDate) {

        if (persons.isEmpty()) {
            return Map.of();
        }

        final List<BlackoutPeriodEntity> overlapping = blackoutPeriodRepository.findOverlapping(startDate, endDate).stream()
            .sorted(comparing(BlackoutPeriodEntity::getStartDate))
            .toList();

        final Map<PersonId, Set<Long>> departmentIdsByPerson = anyDepartmentScoped(overlapping)
            ? departmentService.getDepartmentIdsByMembers(persons)
            : Map.of();

        final Map<Long, VacationType<?>> vacationTypesById = vacationTypesByIdOf(overlapping);
        final Map<BlackoutPeriodEntity, BlackoutPeriod> blackoutPeriodsByEntity = new LinkedHashMap<>();
        overlapping.forEach(entity -> blackoutPeriodsByEntity.put(entity, mapToBlackoutPeriodWithoutDepartments(entity, vacationTypesById)));

        final Map<PersonId, List<BlackoutPeriod>> blackoutPeriodsByPerson = new HashMap<>();
        for (Person person : persons) {
            final Set<Long> departmentIds = departmentIdsByPerson.getOrDefault(person.getIdAsPersonId(), Set.of());
            blackoutPeriodsByPerson.put(person.getIdAsPersonId(), blackoutPeriodsByEntity.entrySet().stream()
                .filter(entry -> appliesToAnyOf(entry.getKey(), departmentIds))
                .map(Map.Entry::getValue)
                .toList());
        }

        return blackoutPeriodsByPerson;
    }

    @Override
    public List<Application> findConflictingApplications(BlackoutPeriod blackoutPeriod) {

        final List<Person> affectedPersons = blackoutPeriod.isCompanyWide()
            ? personService.getActivePersons()
            : blackoutPeriod.getDepartments().stream()
                .flatMap(department -> department.getMembers().stream())
                .distinct()
                .toList();

        if (affectedPersons.isEmpty()) {
            return List.of();
        }

        final List<Application> applications = applicationService.getApplicationsForACertainPeriodAndStatus(
            blackoutPeriod.getStartDate(), blackoutPeriod.getEndDate(), affectedPersons, activeStatuses());

        final List<Application> relevantApplications = blackoutPeriod.appliesToAllVacationTypes()
            ? applications
            : applications.stream().filter(application -> appliesTo(blackoutPeriod, application.getVacationType())).toList();

        return relevantApplications.stream().sorted(comparing(Application::getStartDate)).toList();
    }

    private static boolean anyDepartmentScoped(List<BlackoutPeriodEntity> entities) {
        return entities.stream().anyMatch(entity -> !entity.isCompanyWide());
    }

    /**
     * @return {@code true} if the blackout period is company-wide or scoped to one of the given departments
     */
    private static boolean appliesToAnyOf(BlackoutPeriodEntity entity, Set<Long> departmentIds) {
        return entity.isCompanyWide() || entity.getDepartmentIds().stream().anyMatch(departmentIds::contains);
    }

    private static boolean appliesTo(BlackoutPeriod period, VacationType<?> vacationType) {
        return period.getVacationTypes().stream().anyMatch(type -> type.getId().equals(vacationType.getId()));
    }

    private Map<Long, Department> allDepartmentsById() {
        return departmentService.getAllDepartments().stream().collect(toMap(Department::getId, identity()));
    }

    private Map<Long, VacationType<?>> allVacationTypesById() {
        return vacationTypeService.getAllVacationTypes().stream().collect(toMap(VacationType::getId, identity()));
    }

    /**
     * Resolves the vacation types of the given entities with a single lookup, and without any lookup if none of
     * them is restricted to vacation types.
     */
    private Map<Long, VacationType<?>> vacationTypesByIdOf(List<BlackoutPeriodEntity> entities) {
        final boolean anyRestricted = entities.stream().anyMatch(entity -> !entity.getVacationTypeIds().isEmpty());
        return anyRestricted ? allVacationTypesById() : Map.of();
    }

    private static BlackoutPeriod mapToBlackoutPeriod(BlackoutPeriodEntity entity, Map<Long, Department> departmentsById, Map<Long, VacationType<?>> vacationTypesById) {

        final BlackoutPeriod blackoutPeriod = mapToBlackoutPeriodWithoutDepartments(entity, vacationTypesById);
        blackoutPeriod.setDepartments(entity.getDepartmentIds().stream()
            .map(departmentsById::get)
            .filter(Objects::nonNull)
            .sorted(comparing(department -> department.getName().toLowerCase()))
            .toList());

        return blackoutPeriod;
    }

    /**
     * Maps everything but the departments, which are left empty. Intended for results that are only matched against
     * persons by department ids, so neither departments nor their members and staff need to be loaded.
     */
    private static BlackoutPeriod mapToBlackoutPeriodWithoutDepartments(BlackoutPeriodEntity entity, Map<Long, VacationType<?>> vacationTypesById) {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(entity.getId());
        blackoutPeriod.setTitle(entity.getTitle());
        blackoutPeriod.setStartDate(entity.getStartDate());
        blackoutPeriod.setEndDate(entity.getEndDate());
        blackoutPeriod.setCreatedAt(entity.getCreatedAt());
        blackoutPeriod.setLastModification(entity.getLastModification());
        blackoutPeriod.setCompanyWide(entity.isCompanyWide());
        blackoutPeriod.setAllVacationTypes(entity.isAllVacationTypes());
        blackoutPeriod.setVacationTypes(entity.getVacationTypeIds().stream()
            .<VacationType<?>>map(vacationTypesById::get)
            .filter(Objects::nonNull)
            .toList());

        return blackoutPeriod;
    }

    private static BlackoutPeriodEntity mapToBlackoutPeriodEntity(BlackoutPeriod blackoutPeriod) {

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(blackoutPeriod.getId());
        entity.setTitle(blackoutPeriod.getTitle());
        entity.setStartDate(blackoutPeriod.getStartDate());
        entity.setEndDate(blackoutPeriod.getEndDate());
        entity.setLastModification(blackoutPeriod.getLastModification());
        entity.setCompanyWide(blackoutPeriod.isCompanyWide());
        entity.setAllVacationTypes(blackoutPeriod.appliesToAllVacationTypes());
        entity.setDepartmentIds(blackoutPeriod.getDepartments().stream().map(Department::getId).collect(toSet()));
        entity.setVacationTypeIds(blackoutPeriod.getVacationTypes().stream().map(VacationType::getId).collect(toSet()));

        return entity;
    }

    /**
     * Removes a deleted department from every blackout period. A blackout period that is left without departments
     * is kept - it applies to nobody and is flagged in the blackout period list, so office or boss decide what to do.
     */
    @EventListener
    @Transactional
    void removeDeletedDepartment(DepartmentDeletedEvent event) {

        final Long departmentId = event.departmentId();

        final List<BlackoutPeriodEntity> affected = blackoutPeriodRepository.findAll().stream()
            .filter(entity -> entity.getDepartmentIds().contains(departmentId))
            .toList();

        for (BlackoutPeriodEntity entity : affected) {
            final Set<Long> remainingDepartmentIds = new HashSet<>(entity.getDepartmentIds());
            remainingDepartmentIds.remove(departmentId);
            entity.setDepartmentIds(remainingDepartmentIds);
        }

        blackoutPeriodRepository.saveAll(affected);
    }
}
