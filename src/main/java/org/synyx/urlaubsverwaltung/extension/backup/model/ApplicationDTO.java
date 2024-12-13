package org.synyx.urlaubsverwaltung.extension.backup.model;

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

public record ApplicationDTO(Long id, String personExternalId, String applierExternalId, String bossExternalId,
                             String cancellerExternalId,
                             boolean twoStageApproval, LocalDate startDate, LocalDate endDate,
                             LocalTime startTime, LocalTime endTime, Long vacationTypeId,
                             DayLengthDTO dayLength, String reason,
                             List<HolidayReplacementDTO> holidayReplacements, String address,
                             LocalDate applicationDate, LocalDate cancelDate,
                             LocalDate editedDate, LocalDate remindDate,
                             ApplicationStatusDTO applicationStatus, boolean teamInformed, Duration hours,
                             LocalDate upcomingHolidayReplacementNotificationSend,
                             LocalDate upcomingApplicationsReminderSend,
                             List<ApplicationCommentDTO> applicationComments) {

    public ApplicationEntity toApplicationEntity(VacationTypeEntity vacationType, Person person, Person applier, Person boss, Person canceller, List<HolidayReplacementEntity> holidayReplacements) {
        final ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setPerson(person);
        applicationEntity.setApplier(applier);
        applicationEntity.setBoss(boss);
        applicationEntity.setCanceller(canceller);
        applicationEntity.setTwoStageApproval(this.twoStageApproval());
        applicationEntity.setStartDate(this.startDate());
        applicationEntity.setStartTime(this.startTime());
        applicationEntity.setEndDate(this.endDate());
        applicationEntity.setEndTime(this.endTime());
        applicationEntity.setVacationType(vacationType);
        applicationEntity.setDayLength(DayLength.valueOf(this.dayLength().name()));
        applicationEntity.setReason(this.reason());
        applicationEntity.setHolidayReplacements(holidayReplacements);
        applicationEntity.setAddress(this.address());
        applicationEntity.setApplicationDate(this.applicationDate());
        applicationEntity.setCancelDate(this.cancelDate());
        applicationEntity.setEditedDate(this.editedDate());
        applicationEntity.setRemindDate(this.remindDate());
        applicationEntity.setStatus(ApplicationStatus.valueOf(this.applicationStatus().name()));
        applicationEntity.setTeamInformed(this.teamInformed());
        applicationEntity.setHours(this.hours());
        applicationEntity.setUpcomingHolidayReplacementNotificationSend(this.upcomingHolidayReplacementNotificationSend());
        applicationEntity.setUpcomingApplicationsReminderSend(this.upcomingApplicationsReminderSend());
        return applicationEntity;
    }
}
