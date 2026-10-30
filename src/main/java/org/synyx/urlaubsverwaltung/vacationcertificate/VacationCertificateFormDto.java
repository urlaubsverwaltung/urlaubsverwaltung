package org.synyx.urlaubsverwaltung.vacationcertificate;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

/**
 * The inputs of the vacation certificate the application does not know. They are never stored.
 */
public class VacationCertificateFormDto {

    private Integer year;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate employmentFrom;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate employmentTo;
    private BigDecimal compensatedDays;
    private String employer;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public LocalDate getEmploymentFrom() {
        return employmentFrom;
    }

    public void setEmploymentFrom(LocalDate employmentFrom) {
        this.employmentFrom = employmentFrom;
    }

    public String getEmploymentFromIsoValue() {
        return employmentFrom == null ? "" : employmentFrom.format(ISO_LOCAL_DATE);
    }

    public LocalDate getEmploymentTo() {
        return employmentTo;
    }

    public void setEmploymentTo(LocalDate employmentTo) {
        this.employmentTo = employmentTo;
    }

    public String getEmploymentToIsoValue() {
        return employmentTo == null ? "" : employmentTo.format(ISO_LOCAL_DATE);
    }

    public BigDecimal getCompensatedDays() {
        return compensatedDays;
    }

    public void setCompensatedDays(BigDecimal compensatedDays) {
        this.compensatedDays = compensatedDays;
    }

    public String getEmployer() {
        return employer;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }
}
