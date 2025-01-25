package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.application.vacationtype.CustomVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeLabel;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationCommentActionDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationCommentDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationStatusDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.HolidayReplacementDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.VacationTypeCategoryDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.VacationTypeColorDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.VacationTypeDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SupportedLanguages;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Service
@ConditionalOnBackupCreateEnabled
class ApplicationDataCollectionService {

    private final ApplicationService applicationService;
    private final VacationTypeService vacationTypeService;
    private final ApplicationCommentService applicationCommentService;

    ApplicationDataCollectionService(ApplicationService applicationService, ApplicationCommentService applicationCommentService, VacationTypeService vacationTypeService) {
        this.applicationService = applicationService;
        this.applicationCommentService = applicationCommentService;
        this.vacationTypeService = vacationTypeService;
    }

    ApplicationBackupDTO collectApplications(List<Person> allPersons, LocalDate from, LocalDate to) {

        final List<VacationTypeDTO> vacationTypes = vacationTypeService.getAllVacationTypes().stream()
            .map(this::createVacationTypeDTO)
            .toList();

        final List<ApplicationDTO> applications = allPersons.stream()
            .map(person ->
                applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person).stream()
                    .map(application -> {

                        final List<HolidayReplacementDTO> holidayReplacements = application.getHolidayReplacements().stream()
                            .map(holidayReplacementEntity -> new HolidayReplacementDTO(holidayReplacementEntity.getPerson().getUsername(), holidayReplacementEntity.getNote()))
                            .toList();

                        final List<ApplicationCommentDTO> applicationComments = applicationCommentService.getCommentsByApplication(application).stream()
                            .map(applicationComment -> new ApplicationCommentDTO(ApplicationCommentActionDTO.valueOf(applicationComment.action().name()), optionalExternalUserId(applicationComment.person()), applicationComment.date(), applicationComment.text()))
                            .toList();

                        return createApplicationDTO(application, holidayReplacements, applicationComments);
                    })
                    .toList()
            )
            .flatMap(Collection::stream)
            .toList();

        return new ApplicationBackupDTO(vacationTypes, applications);
    }

    private VacationTypeDTO createVacationTypeDTO(VacationType<?> vacationType) {

        String messageKey = null;
        if (vacationType instanceof ProvidedVacationType providedVacationType) {
            messageKey = providedVacationType.getMessageKey();
        }

        return new VacationTypeDTO(
            vacationType.getId(),
            vacationType.isActive(),
            VacationTypeCategoryDTO.valueOf(vacationType.getCategory().name()),
            vacationType.isRequiresApprovalToApply(),
            vacationType.isRequiresApprovalToCancel(),
            VacationTypeColorDTO.valueOf(vacationType.getColor().name()),
            vacationType.isVisibleToEveryone(),
            vacationType instanceof CustomVacationType,
            messageKey,
            toLabels(vacationType)
        );
    }

    private static ApplicationDTO createApplicationDTO(
        Application application,
        List<HolidayReplacementDTO> holidayReplacements,
        List<ApplicationCommentDTO> applicationComments
    ) {

        final String applierExternalId = optionalExternalUserId(application.getApplier());
        final String bossExternalId = optionalExternalUserId(application.getBoss());
        final String cancellerExternalId = optionalExternalUserId(application.getCanceller());

        return new ApplicationDTO(application.getId(),
            application.getPerson().getUsername(),
            applierExternalId,
            bossExternalId,
            cancellerExternalId,
            application.isTwoStageApproval(),
            application.getStartDate(),
            application.getEndDate(),
            application.getStartTime(),
            application.getEndTime(),
            application.getVacationType().getId(),
            DayLengthDTO.valueOf(application.getDayLength().name()),
            application.getReason(),
            holidayReplacements,
            application.getAddress(),
            application.getApplicationDate(),
            application.getCancelDate(),
            application.getEditedDate(),
            application.getRemindDate(),
            ApplicationStatusDTO.valueOf(application.getStatus().name()),
            application.isTeamInformed(),
            application.getHours(),
            application.getUpcomingHolidayReplacementNotificationSend(),
            application.getUpcomingApplicationsReminderSend(),
            applicationComments
        );
    }

    private Map<Locale, String> toLabels(VacationType<?> vacationType) {
        return switch (vacationType) {
            case ProvidedVacationType providedVacationType -> toLabels(providedVacationType);
            case CustomVacationType customVacationType -> toLabels(customVacationType);
            default -> throw new IllegalArgumentException("Unsupported vacation type: " + vacationType);
        };
    }

    private Map<Locale, String> toLabels(ProvidedVacationType providedVacationType) {
        return Arrays.stream(SupportedLanguages.values()).map(SupportedLanguages::getLocale).collect(toMap(identity(), providedVacationType::getLabel));
    }

    private Map<Locale, String> toLabels(CustomVacationType customVacationType) {
        return customVacationType.labelsByLocale().values().stream().collect(toMap(VacationTypeLabel::locale, VacationTypeLabel::label));
    }

    private static String optionalExternalUserId(Person person) {
        return person != null ? person.getUsername() : null;
    }
}
