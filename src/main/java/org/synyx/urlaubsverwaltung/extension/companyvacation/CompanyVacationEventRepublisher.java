package org.synyx.urlaubsverwaltung.extension.companyvacation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.companyvacation.CompanyVacationService;

/**
 * Used for different use cases
 * - republishing events on application start (single tenant)
 * - republishing events on turn of year
 */
@Component
@ConditionalOnProperty(value = "uv.extensions.settings.republish.enabled", havingValue = "true")
@ConditionalOnBean(CompanyVacationEventHandlerExtension.class)
public class CompanyVacationEventRepublisher {

    private final CompanyVacationService companyVacationService;

    CompanyVacationEventRepublisher(
        CompanyVacationService companyVacationService
    ) {
        this.companyVacationService = companyVacationService;
    }

    public void republishEvents() {
        companyVacationService.publishCompanyEvents();
    }
}
