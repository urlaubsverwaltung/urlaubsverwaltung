package org.synyx.urlaubsverwaltung.dev;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.time.Clock;

@Configuration
@ConditionalOnProperty(value = "uv.development.demodata.create", havingValue = "true")
@EnableConfigurationProperties(DemoDataProperties.class)
class DemoDataConfiguration {

    @Bean
    DemoDataCreationService demoDataCreationService(PersonDataProvider personDataProvider, ApplicationForLeaveDataProvider applicationForLeaveDataProvider,
                                                    SickNoteDataProvider sickNoteDataProvider, OvertimeRecordDataProvider overtimeRecordDataProvider,
                                                    DepartmentDataProvider departmentDataProvider, Clock clock) {
        return new DemoDataCreationService(personDataProvider, applicationForLeaveDataProvider, sickNoteDataProvider, overtimeRecordDataProvider, departmentDataProvider, clock);
    }

    @Bean
    PersonCreatedEventListener personCreatedEventListener(DemoDataCreationService demoDataCreationService) {
        return new PersonCreatedEventListener(demoDataCreationService);
    }

    @Bean
    SickNoteDataProvider sickNoteDataProvider(SickNoteInteractionService sickNoteInteractionService, DurationChecker durationChecker, SickNoteTypeService sickNoteTypeService) {
        return new SickNoteDataProvider(sickNoteInteractionService, durationChecker, sickNoteTypeService);
    }

    @Bean
    PersonDataProvider personDataProvider(PersonService personService, PersonBasedataService personBasedataService, WorkingTimeWriteService workingTimeWriteService, AccountInteractionService accountInteractionService, Clock clock) {
        return new PersonDataProvider(personService, personBasedataService, workingTimeWriteService, accountInteractionService, clock);
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
