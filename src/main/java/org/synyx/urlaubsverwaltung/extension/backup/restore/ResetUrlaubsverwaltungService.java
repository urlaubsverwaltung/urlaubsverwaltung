package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.AccountImportService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationImportService;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentImportService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeImportService;
import org.synyx.urlaubsverwaltung.calendar.CalendarAccessibleImportService;
import org.synyx.urlaubsverwaltung.calendar.CompanyCalendarImportService;
import org.synyx.urlaubsverwaltung.calendar.PersonCalendarImportService;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingImportService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarIntegrationSettingsImportService;
import org.synyx.urlaubsverwaltung.department.DepartmentImportService;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsImportService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeImportService;
import org.synyx.urlaubsverwaltung.person.PersonImportService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBaseDataImportService;
import org.synyx.urlaubsverwaltung.settings.SettingsImportService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentImportService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteImportService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionImportService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeImportService;
import org.synyx.urlaubsverwaltung.user.UserSettingsImportService;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsImportService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeImportService;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class ResetUrlaubsverwaltungService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final AccountImportService accountImportService;
    private final ApplicationCommentImportService applicationCommentImportService;
    private final ApplicationImportService applicationImportService;
    private final CalendarAccessibleImportService calendarAccessibleImportService;
    private final CalendarIntegrationSettingsImportService calendarIntegrationSettingsImportService;
    private final CompanyCalendarImportService companyCalendarImportService;
    private final DepartmentImportService departmentImportService;
    private final OvertimeImportService overtimeImportService;
    private final PersonCalendarImportService personCalendarImportService;
    private final SettingsImportService settingsImportService;
    private final SickNoteImportService sickNoteImportService;
    private final VacationTypeImportService vacationTypeImportService;
    private final WorkingTimeImportService workingTimeImportService;
    private final AbsenceMappingImportService absenceMappingImportService;
    private final PersonImportService personImportService;
    private final UserSettingsImportService userSettingsImportService;
    private final PersonBaseDataImportService personBaseDataImportService;
    private final SickNoteExtensionImportService sickNoteExtensionImportService;
    private final SickNoteCommentImportService sickNoteCommentImportService;
    private final SickNoteTypeImportService sickNoteTypeImportService;
    private final UserNotificationSettingsImportService userNotificationSettingsImportService;
    private final UserPaginationSettingsImportService userPaginationSettingsImportService;

    ResetUrlaubsverwaltungService(AccountImportService accountImportService,
                                  ApplicationCommentImportService applicationCommentImportService,
                                  ApplicationImportService applicationImportService,
                                  CalendarAccessibleImportService calendarAccessibleImportService,
                                  CalendarIntegrationSettingsImportService calendarIntegrationSettingsImportService,
                                  CompanyCalendarImportService companyCalendarImportService,
                                  DepartmentImportService departmentImportService,
                                  OvertimeImportService overtimeImportService,
                                  PersonCalendarImportService personCalendarImportService,
                                  SettingsImportService settingsImportService,
                                  SickNoteImportService sickNoteImportService,
                                  VacationTypeImportService vacationTypeImportService,
                                  WorkingTimeImportService workingTimeImportService,
                                  AbsenceMappingImportService absenceMappingImportService,
                                  PersonImportService personImportService,
                                  UserSettingsImportService userSettingsImportService,
                                  PersonBaseDataImportService personBaseDataImportService,
                                  SickNoteExtensionImportService sickNoteExtensionImportService,
                                  SickNoteCommentImportService sickNoteCommentImportService,
                                  SickNoteTypeImportService sickNoteTypeImportService,
                                  UserNotificationSettingsImportService userNotificationSettingsImportService,
                                  UserPaginationSettingsImportService userPaginationSettingsImportService
    ) {
        this.accountImportService = accountImportService;
        this.applicationCommentImportService = applicationCommentImportService;
        this.applicationImportService = applicationImportService;
        this.calendarAccessibleImportService = calendarAccessibleImportService;
        this.calendarIntegrationSettingsImportService = calendarIntegrationSettingsImportService;
        this.companyCalendarImportService = companyCalendarImportService;
        this.departmentImportService = departmentImportService;
        this.overtimeImportService = overtimeImportService;
        this.personCalendarImportService = personCalendarImportService;
        this.settingsImportService = settingsImportService;
        this.sickNoteImportService = sickNoteImportService;
        this.vacationTypeImportService = vacationTypeImportService;
        this.workingTimeImportService = workingTimeImportService;
        this.absenceMappingImportService = absenceMappingImportService;
        this.personImportService = personImportService;
        this.userSettingsImportService = userSettingsImportService;
        this.personBaseDataImportService = personBaseDataImportService;
        this.sickNoteExtensionImportService = sickNoteExtensionImportService;
        this.sickNoteCommentImportService = sickNoteCommentImportService;
        this.sickNoteTypeImportService = sickNoteTypeImportService;
        this.userNotificationSettingsImportService = userNotificationSettingsImportService;
        this.userPaginationSettingsImportService = userPaginationSettingsImportService;
    }


    void resetData() {
        LOG.info("Resetting all data...");
        departmentImportService.deleteAll();
        sickNoteExtensionImportService.deleteAll();
        sickNoteCommentImportService.deleteAll();
        sickNoteImportService.deleteAll();
        sickNoteTypeImportService.deleteAll();
        personCalendarImportService.deleteAll();
        calendarAccessibleImportService.deleteAll();
        companyCalendarImportService.deleteAll();
        applicationCommentImportService.deleteAll();
        applicationImportService.deleteAll();
        vacationTypeImportService.deleteAll();
        overtimeImportService.deleteAll();
        accountImportService.deleteAll();
        workingTimeImportService.deleteAll();
        personBaseDataImportService.deleteAll();
        userPaginationSettingsImportService.deleteAll();
        userNotificationSettingsImportService.deleteAll();
        userSettingsImportService.deleteAll();
        personImportService.deleteAll();
        calendarIntegrationSettingsImportService.deleteAll();
        absenceMappingImportService.deleteAll();
        settingsImportService.deleteAll();
        LOG.info("All data is gone ...");
    }
}
