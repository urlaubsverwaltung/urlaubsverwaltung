package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.stereotype.Service;

@Service
public class CompanyCalendarImportService {

    private final CompanyCalendarRepository companyCalendarRepository;

    CompanyCalendarImportService(CompanyCalendarRepository companyCalendarRepository) {
        this.companyCalendarRepository = companyCalendarRepository;
    }

    public void deleteAll() {
        companyCalendarRepository.deleteAll();
    }

    public void importCompanyCalendar(CompanyCalendar companyCalendar) {
        companyCalendarRepository.save(companyCalendar);
    }
}
