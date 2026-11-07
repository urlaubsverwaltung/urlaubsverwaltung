package org.synyx.urlaubsverwaltung.companyclosure.api;

import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosure;

import java.math.BigDecimal;

public final class CompanyClosureDto {

    private final String date;
    private final String description;
    private final BigDecimal dayLength;
    private final String absencePeriodName;

    CompanyClosureDto(CompanyClosure companyClosure) {
        this.date = companyClosure.date().toString();
        this.description = companyClosure.name();
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
