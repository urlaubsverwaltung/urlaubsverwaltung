package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.synyx.urlaubsverwaltung.person.PersonPermissionsChangedEvent;

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Component
class PersonPermissionsChangedListener {

    private final CompanyCalendarService companyCalendarService;
    private final CalendarAccessibleService calendarAccessibleService;

    @Autowired
    PersonPermissionsChangedListener(CompanyCalendarService companyCalendarService, CalendarAccessibleService calendarAccessibleService) {
        this.companyCalendarService = companyCalendarService;
        this.calendarAccessibleService = calendarAccessibleService;
    }

    // like PersonDisabledListener: the event is published within the update transaction,
    // the person must be visible to the @Async thread before its calendar is deleted.
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void handlePersonPermissionsChangedEvent(PersonPermissionsChangedEvent event) {

        final boolean isBossOrOffice = event.currentPermissions().contains(BOSS) || event.currentPermissions().contains(OFFICE);
        if (!isBossOrOffice && !calendarAccessibleService.isCompanyCalendarAccessible()) {
            companyCalendarService.deleteCalendarForPerson(event.personId());
        }
    }
}
