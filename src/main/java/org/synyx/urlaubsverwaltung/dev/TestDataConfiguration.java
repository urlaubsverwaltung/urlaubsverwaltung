package org.synyx.urlaubsverwaltung.dev;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

@Configuration
@ConditionalOnProperty(value = "uv.development.testdata.create", havingValue = "true")
@EnableConfigurationProperties(TestDataProperties.class)
class TestDataConfiguration {

    @Bean
    TestDataCreationService testDataCreationService(PersonDataProvider personDataProvider, ApplicationForLeaveDataProvider applicationForLeaveDataProvider,
                                                    SickNoteDataProvider sickNoteDataProvider, SickNoteTypeService sickNoteTypeService,
                                                    VacationTypeService vacationTypeService, OvertimeRecordDataProvider overtimeRecordDataProvider,
                                                    DepartmentDataProvider departmentDataProvider, TestDataProperties testDataProperties) {
        return new TestDataCreationService(personDataProvider, applicationForLeaveDataProvider, sickNoteDataProvider, sickNoteTypeService, vacationTypeService, overtimeRecordDataProvider, departmentDataProvider, testDataProperties);
    }

    @Bean
    SickNoteDataProvider sickNoteDataProvider(SickNoteInteractionService sickNoteInteractionService, DurationChecker durationChecker) {
        return new SickNoteDataProvider(sickNoteInteractionService, durationChecker);
    }

    @Bean
    PersonDataProvider personDataProvider(PersonService personService, WorkingTimeService workingTimeService,
                                          AccountInteractionService accountInteractionService) {
        return new PersonDataProvider(personService, workingTimeService, accountInteractionService);
    }

    @Bean
    OvertimeRecordDataProvider overtimeRecordDataProvider(OvertimeService overtimeService, SettingsService settingsService) {
        return new OvertimeRecordDataProvider(overtimeService, settingsService);
    }

    @Bean
    DurationChecker durationChecker(WorkDaysService workDaysService) {
        return new DurationChecker(workDaysService);
    }

    @Bean
    DepartmentDataProvider departmentDataProvider(DepartmentService departmentService) {
        return new DepartmentDataProvider(departmentService);
    }

    @Bean
    ApplicationForLeaveDataProvider applicationForLeaveDataProvider(ApplicationInteractionService applicationInteractionService,
                                                                    DurationChecker durationChecker) {
        return new ApplicationForLeaveDataProvider(applicationInteractionService, durationChecker);
    }
}
