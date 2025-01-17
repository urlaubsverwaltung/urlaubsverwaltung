package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.application.ApplicationEntity;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.application.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationDTOTest {

    @Test
    void happyPath() {
        ApplicationDTO applicationDTO = new ApplicationDTO(1L, "personExternalId", "applierExternalId", "bossExternalId", "cancellerExternalId", true, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 10), LocalTime.of(9, 0), LocalTime.of(17, 0), 1L, DayLengthDTO.FULL, "Vacation", List.of(), "Address", LocalDate.of(2023, 1, 1), null, null, null, ApplicationStatusDTO.ALLOWED, true, Duration.ofHours(8), null, null, List.of());
        VacationTypeEntity vacationType = new VacationTypeEntity();
        Person person = new Person();
        Person applier = new Person();
        Person boss = new Person();
        Person canceller = new Person();
        List<HolidayReplacementEntity> holidayReplacements = List.of();

        ApplicationEntity applicationEntity = applicationDTO.toApplicationEntity(vacationType, person, applier, boss, canceller, holidayReplacements);

        assertThat(applicationEntity.getPerson()).isEqualTo(person);
        assertThat(applicationEntity.getApplier()).isEqualTo(applier);
        assertThat(applicationEntity.getBoss()).isEqualTo(boss);
        assertThat(applicationEntity.getCanceller()).isEqualTo(canceller);
        assertThat(applicationEntity.isTwoStageApproval()).isEqualTo(applicationDTO.twoStageApproval());
        assertThat(applicationEntity.getStartDate()).isEqualTo(applicationDTO.startDate());
        assertThat(applicationEntity.getStartTime()).isEqualTo(applicationDTO.startTime());
        assertThat(applicationEntity.getEndDate()).isEqualTo(applicationDTO.endDate());
        assertThat(applicationEntity.getEndTime()).isEqualTo(applicationDTO.endTime());
        assertThat(applicationEntity.getVacationType()).isEqualTo(vacationType);
        assertThat(applicationEntity.getDayLength()).isEqualTo(DayLength.valueOf(applicationDTO.dayLength().name()));
        assertThat(applicationEntity.getReason()).isEqualTo(applicationDTO.reason());
        assertThat(applicationEntity.getHolidayReplacements()).isEqualTo(holidayReplacements);
        assertThat(applicationEntity.getAddress()).isEqualTo(applicationDTO.address());
        assertThat(applicationEntity.getApplicationDate()).isEqualTo(applicationDTO.applicationDate());
        assertThat(applicationEntity.getCancelDate()).isEqualTo(applicationDTO.cancelDate());
        assertThat(applicationEntity.getEditedDate()).isEqualTo(applicationDTO.editedDate());
        assertThat(applicationEntity.getRemindDate()).isEqualTo(applicationDTO.remindDate());
        assertThat(applicationEntity.getStatus()).isEqualTo(ApplicationStatus.valueOf(applicationDTO.applicationStatus().name()));
        assertThat(applicationEntity.isTeamInformed()).isEqualTo(applicationDTO.teamInformed());
        assertThat(applicationEntity.getHours()).isEqualTo(applicationDTO.hours());
        assertThat(applicationEntity.getUpcomingHolidayReplacementNotificationSend()).isEqualTo(applicationDTO.upcomingHolidayReplacementNotificationSend());
        assertThat(applicationEntity.getUpcomingApplicationsReminderSend()).isEqualTo(applicationDTO.upcomingApplicationsReminderSend());
    }

}
