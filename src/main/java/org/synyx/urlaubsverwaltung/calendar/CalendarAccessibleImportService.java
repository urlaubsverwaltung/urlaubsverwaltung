package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.stereotype.Service;

@Service
public class CalendarAccessibleImportService {

    private final CompanyCalendarAccessibleRepository companyCalendarAccessibleRepository;

    CalendarAccessibleImportService(CompanyCalendarAccessibleRepository companyCalendarAccessibleRepository) {
        this.companyCalendarAccessibleRepository = companyCalendarAccessibleRepository;
    }

    public void deleteAll() {
        companyCalendarAccessibleRepository.deleteAll();
    }

    public void importCompanyCalendarAccessible(CompanyCalendarAccessible entity) {
        companyCalendarAccessibleRepository.save(entity);
    }
}
