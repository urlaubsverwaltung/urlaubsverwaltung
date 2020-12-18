package org.synyx.urlaubsverwaltung.dev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityConfigurationProperties;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;

@Configuration
@ConditionalOnProperty(value = "uv.development.demodata.create", havingValue = "true")
@EnableConfigurationProperties(DemoDataProperties.class)
class DemoDataConfiguration {

    @Bean
    DemoDataCreationService demoDataCreationService(PersonDataProvider personDataProvider, ApplicationForLeaveDataProvider applicationForLeaveDataProvider,
                                                    SickNoteDataProvider sickNoteDataProvider, OvertimeRecordDataProvider overtimeRecordDataProvider,
                                                    DepartmentDataProvider departmentDataProvider, DemoDataProperties demoDataProperties, Clock clock) {
        return new DemoDataCreationService(personDataProvider, applicationForLeaveDataProvider, sickNoteDataProvider, overtimeRecordDataProvider, departmentDataProvider, demoDataProperties, clock);
    }

    @Bean
    SickNoteDataProvider sickNoteDataProvider(SickNoteInteractionService sickNoteInteractionService, DurationChecker durationChecker, SickNoteTypeService sickNoteTypeService) {
        return new SickNoteDataProvider(sickNoteInteractionService, durationChecker, sickNoteTypeService);
    }

    @Bean
    PersonDataProvider personDataProvider(PersonService personService, WorkingTimeService workingTimeService, AccountInteractionService accountInteractionService, Clock clock,
                                          SecurityConfigurationProperties properties) {

        boolean isOauth2Enabled = "oidc".equalsIgnoreCase(properties.getAuth().name());

        return new PersonDataProvider(personService, workingTimeService, accountInteractionService, clock, isOauth2Enabled);
    }

    @Bean
    OvertimeRecordDataProvider overtimeRecordDataProvider(OvertimeService overtimeService, SettingsService settingsService) {
        return new OvertimeRecordDataProvider(overtimeService, settingsService);
    }

    @Bean
    DurationChecker durationChecker(WorkDaysCountService workDaysCountService, Clock clock) {
        return new DurationChecker(workDaysCountService, clock);
    }

    @Bean
    DepartmentDataProvider departmentDataProvider(DepartmentService departmentService, Clock clock) {
        return new DepartmentDataProvider(departmentService, clock);
    }

    @Bean
    ApplicationForLeaveDataProvider applicationForLeaveDataProvider(ApplicationInteractionService applicationInteractionService, DurationChecker durationChecker, VacationTypeService vacationTypeService) {
        return new ApplicationForLeaveDataProvider(applicationInteractionService, durationChecker, vacationTypeService);
    }
}
