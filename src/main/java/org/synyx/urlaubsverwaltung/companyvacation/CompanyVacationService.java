package org.synyx.urlaubsverwaltung.companyvacation;

public interface CompanyVacationService {

    /**
     * Republishes all known company vacations via {@link CompanyVacationPublishedEvent}.
     */
    void publishCompanyEvents();
}
