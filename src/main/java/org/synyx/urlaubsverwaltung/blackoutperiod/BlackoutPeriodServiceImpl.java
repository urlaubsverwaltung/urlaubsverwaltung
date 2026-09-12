package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.LocalDate;
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

        final Set<Long> personDepartmentIds = departmentService.getAssignedDepartmentsOfMember(person).stream()
            .map(Department::getId)
            .collect(toSet());

        return getAllBlackoutPeriods().stream()
            .filter(period -> period.overlaps(startDate, endDate))
            .filter(period -> period.isCompanyWide() || appliesToAnyOf(period, personDepartmentIds))
            .filter(period -> period.appliesToAllVacationTypes() || appliesTo(period, vacationType))
            .min(comparing(BlackoutPeriod::getStartDate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlackoutPeriod> findBlackoutPeriodsForPerson(Person person, LocalDate startDate, LocalDate endDate) {

        final Set<Long> personDepartmentIds = departmentService.getAssignedDepartmentsOfMember(person).stream()
            .map(Department::getId)
            .collect(toSet());

        return getAllBlackoutPeriods().stream()
            .filter(period -> period.overlaps(startDate, endDate))
            .filter(period -> period.isCompanyWide() || appliesToAnyOf(period, personDepartmentIds))
            .sorted(comparing(BlackoutPeriod::getStartDate))
            .toList();
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

    private static boolean appliesToAnyOf(BlackoutPeriod period, Set<Long> departmentIds) {
        return period.getDepartments().stream().map(Department::getId).anyMatch(departmentIds::contains);
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

    private static BlackoutPeriod mapToBlackoutPeriod(BlackoutPeriodEntity entity, Map<Long, Department> departmentsById, Map<Long, VacationType<?>> vacationTypesById) {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(entity.getId());
        blackoutPeriod.setTitle(entity.getTitle());
        blackoutPeriod.setStartDate(entity.getStartDate());
        blackoutPeriod.setEndDate(entity.getEndDate());
        blackoutPeriod.setCreatedAt(entity.getCreatedAt());
        blackoutPeriod.setLastModification(entity.getLastModification());
        blackoutPeriod.setDepartments(entity.getDepartmentIds().stream()
            .map(departmentsById::get)
            .filter(Objects::nonNull)
            .sorted(comparing(department -> department.getName().toLowerCase()))
            .toList());
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
        entity.setDepartmentIds(blackoutPeriod.getDepartments().stream().map(Department::getId).collect(toSet()));
        entity.setVacationTypeIds(blackoutPeriod.getVacationTypes().stream().map(VacationType::getId).collect(toSet()));

        return entity;
    }
}
