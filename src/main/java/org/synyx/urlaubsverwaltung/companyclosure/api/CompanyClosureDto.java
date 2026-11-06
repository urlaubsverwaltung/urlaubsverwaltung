package org.synyx.urlaubsverwaltung.companyclosure.api;

import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosure;

import java.math.BigDecimal;
import java.util.Locale;

public final class CompanyClosureDto {

    private final String date;
    private final String description;
    private final BigDecimal dayLength;
    private final String absencePeriodName;

    CompanyClosureDto(CompanyClosure companyClosure, Locale locale) {
        this.date = companyClosure.date().toString();
        this.description = companyClosure.name(locale);
        this.dayLength = companyClosure.dayLength().getDuration();
        this.absencePeriodName = companyClosure.dayLength().name();
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDayLength() {
        return dayLength;
    }

    public String getAbsencePeriodName() {
        return absencePeriodName;
    }
}
