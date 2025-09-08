package org.synyx.urlaubsverwaltung.extension.backup.backup;

import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CalendarBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CalendarIntegrationBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentMembershipDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.OvertimeDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.UrlaubsverwaltungBackupDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupCreateEnabled
class BackupDataCollectionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final String applicationVersion;
    private final TenantSupplier tenantSupplier;
    private final PersonService personService;
    private final CalendarIntegrationDataCollectionService calendarIntegrationDataCollectionService;
    private final CalendarDataCollectionService calendarDataCollectionService;
    private final SettingsDataCollectionService settingsDataCollectionService;
    private final DepartmentDataCollectionService departmentDataCollectionService;
    private final DepartmentMembershipDataCollectionService departmentMembershipDataCollectionService;
    private final ApplicationDataCollectionService applicationDataCollectionService;
    private final SickNoteDataCollectionService sickNoteDataCollectionService;
    private final OvertimeDataCollectionService overtimeDataCollectionService;
    private final PersonDataCollectionService personDataCollectionService;

    BackupDataCollectionService(@Value("${info.app.version}") String applicationVersion,
                                TenantSupplier tenantSupplier,
                                PersonService personService,
                                CalendarIntegrationDataCollectionService calendarIntegrationDataCollectionService,
                                CalendarDataCollectionService calendarDataCollectionService,
                                SettingsDataCollectionService settingsDataCollectionService,
                                DepartmentDataCollectionService departmentDataCollectionService,
                                DepartmentMembershipDataCollectionService departmentMembershipDataCollectionService,
                                ApplicationDataCollectionService applicationDataCollectionService,
                                SickNoteDataCollectionService sickNoteDataCollectionService,
                                OvertimeDataCollectionService overtimeDataCollectionService,
                                PersonDataCollectionService personDataCollectionService) {
        this.applicationVersion = applicationVersion;
        this.tenantSupplier = tenantSupplier;
        this.personService = personService;
        this.calendarIntegrationDataCollectionService = calendarIntegrationDataCollectionService;
        this.calendarDataCollectionService = calendarDataCollectionService;
        this.settingsDataCollectionService = settingsDataCollectionService;
        this.departmentDataCollectionService = departmentDataCollectionService;
        this.departmentMembershipDataCollectionService = departmentMembershipDataCollectionService;
        this.applicationDataCollectionService = applicationDataCollectionService;
        this.sickNoteDataCollectionService = sickNoteDataCollectionService;
        this.overtimeDataCollectionService = overtimeDataCollectionService;
        this.personDataCollectionService = personDataCollectionService;
    }

    private static LocalDate getLastDayOfNextYear() {
        return LocalDate.of(LocalDate.now().plusYears(1).getYear(), 12, 31);
    }

    public UrlaubsverwaltungBackupDTO collectData() {
        LOG.info("Collection data for backup ...");

        final LocalDate exportFrom = LocalDate.ofEpochDay(0);
        final LocalDate exportTo = getLastDayOfNextYear();

        final List<Person> allPersons = personService.getAllPersons();
        final Map<PersonId, Person> personById = allPersons.stream().collect(toMap(Person::getIdAsPersonId, identity()));

        final List<PersonDTO> personDtos = personDataCollectionService.collectPersons(allPersons);
        final Map<PersonId, PersonDTO> personDTOById = personDtos.stream().collect(toMap(dto -> new PersonId(dto.id()), identity()));

        final List<OvertimeDTO> overtimes = overtimeDataCollectionService.collectOvertimes(personDtos, personById::get);
        final SickNoteBackupDTO sickNotes = sickNoteDataCollectionService.collectSickNotes(allPersons, exportFrom, exportTo);
        final ApplicationBackupDTO applications = applicationDataCollectionService.collectApplications(allPersons, exportFrom, exportTo);
        final List<DepartmentDTO> departments = departmentDataCollectionService.collectDepartments();
        final List<DepartmentMembershipDTO> departmentMemberships = departmentMembershipDataCollectionService.collectDepartmentMemberships(personDTOById::get);
        final SettingsDTO settings = settingsDataCollectionService.collectSettings();
        final CalendarBackupDTO calendars = calendarDataCollectionService.collectCalendars(allPersons);
        final CalendarIntegrationBackupDTO calendarIntegration = calendarIntegrationDataCollectionService.collectCalendarIntegration();

        LOG.info("Collected data for backup");

        return new UrlaubsverwaltungBackupDTO(tenantSupplier.get(), applicationVersion, personDtos, overtimes, sickNotes,
            applications, departments, departmentMemberships, calendars, calendarIntegration, settings);
    }
}
