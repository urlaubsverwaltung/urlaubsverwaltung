package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.application.ApplicationEntity;
import org.synyx.urlaubsverwaltung.application.application.ApplicationImportService;
import org.synyx.urlaubsverwaltung.application.application.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentImportService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeImportService;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationCommentDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnBackupRestoreEnabled
class ApplicationRestoreService {

    private final ApplicationCommentImportService applicationCommentImportService;
    private final ApplicationImportService applicationImportService;
    private final VacationTypeImportService vacationTypeImportService;
    private final PersonService personService;

    ApplicationRestoreService(ApplicationCommentImportService applicationCommentImportService, ApplicationImportService applicationImportService, VacationTypeImportService vacationTypeImportService, PersonService personService) {
        this.applicationCommentImportService = applicationCommentImportService;
        this.applicationImportService = applicationImportService;
        this.vacationTypeImportService = vacationTypeImportService;
        this.personService = personService;
    }

    /**
     * @param createdVacationTypes list of already imported vacationTypes also including the id of the vacation type id in the backup
     * @param originVacationTypeId the id of the vacation type in the backup
     * @return
     */
    private static Optional<VacationTypeTuple> resolveCreatedVacationTypeByOriginId(List<VacationTypeTuple> createdVacationTypes, Long originVacationTypeId) {
        return createdVacationTypes.stream().filter(tuple -> tuple.idOfImport.equals(originVacationTypeId)).findFirst();
    }

    List<ImportedIdTuple> restore(ApplicationBackupDTO applications) {
        List<VacationTypeTuple> createdVacationTypes = importVacationTypes(applications);
        return importApplications(applications, createdVacationTypes);
    }

    private List<ImportedIdTuple> importApplications(ApplicationBackupDTO applications, List<VacationTypeTuple> createdVacationTypes) {
        return applications.applications().stream().map(applicationDTO -> resolveCreatedVacationTypeByOriginId(createdVacationTypes, applicationDTO.vacationTypeId()).map(vacationTypeTuple -> {
            final ApplicationEntity createdApplicationEntity = importApplication(applicationDTO, vacationTypeTuple.createdVacationType);
            importApplicationComments(applicationDTO.applicationComments(), createdApplicationEntity);
            return new ImportedIdTuple(applicationDTO.id(), createdApplicationEntity.getId());
        }).orElseThrow(() ->
            // somehow we could not import given application to vacation type with origin Id = applicationDTO.vacationTypeId()
            new IllegalStateException("Could not find vacation type with id " + applicationDTO.vacationTypeId()))
        ).toList();
    }

    private List<VacationTypeTuple> importVacationTypes(ApplicationBackupDTO applications) {
        return applications.vacationTypes().stream().map(vacationType -> {
            VacationTypeEntity createdVacationType = vacationTypeImportService.importVacationType(vacationType.toVacationType());
            return new VacationTypeTuple(vacationType.id(), createdVacationType);
        }).toList();
    }

    private void importApplicationComments(List<ApplicationCommentDTO> applicationComments, ApplicationEntity createdApplicationEntity) {
        applicationComments.forEach(comment -> {
            final Person author = getPerson(comment.externalId());
            applicationCommentImportService.importApplicationComment(comment.toApplicationCommentEntity(author, createdApplicationEntity.getId()));
        });
    }

    private ApplicationEntity importApplication(ApplicationDTO applicationDTO, VacationTypeEntity createdVacationType) {
        final List<HolidayReplacementEntity> holidayReplacements = applicationDTO.holidayReplacements().stream().map(replacementDTO -> {
            final Person person = getPerson(replacementDTO.externalId());
            return replacementDTO.toHolidayReplacementEntity(person);
        }).toList();

        final Person person = getPerson(applicationDTO.personExternalId());
        final Person applier = getPerson(applicationDTO.applierExternalId());
        final Person boss = getPerson(applicationDTO.bossExternalId());
        final Person canceller = getPerson(applicationDTO.cancellerExternalId());

        final ApplicationEntity applicationEntity = applicationDTO.toApplicationEntity(createdVacationType, person, applier, boss, canceller, holidayReplacements);

        return applicationImportService.importApplication(applicationEntity);
    }

    /**
     * Get a person by external id aka username, when a person is not found a runtime exception is thrown.
     *
     * @param externalId external id / username
     * @return
     */
    private Person getPerson(String externalId) {
        return externalId == null ? null : personService.getPersonByUsername(externalId).orElseThrow();
    }

    private record VacationTypeTuple(Long idOfImport, VacationTypeEntity createdVacationType) {
    }
}
