package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Locale.forLanguageTag;
import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

/**
 * Day and month are only used to create a closure day, an existing one is identified by the url.
 * <p>
 * Serializable, as a rejected form is kept in the session for the redirect back to the form.
 */
public class CompanyClosureFormDto implements Serializable {

    private Integer day;
    private Integer month;
    /**
     * one name per supported language in the order of {@link org.synyx.urlaubsverwaltung.settings.SupportedLanguages}
     */
    private List<CompanyClosureNameFormDto> names = new ArrayList<>();
    /**
     * day length and "valid from" of a new closure day, or of a new version of an existing one. For an existing
     * closure day the day length starts with the one valid today and "valid from" empty, see {@link #hasNewVersion}.
     */
    private DayLength dayLength;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate validFrom;
    /**
     * the versions of an existing closure day that have not started yet, the latest first
     */
    private List<CompanyClosureVersionFormDto> versions = new ArrayList<>();

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    /**
     * Errors that concern day and month together are attached to this property.
     *
     * @return the day of the year, {@code null} while day or month are missing or do not form an existing day
     */
    public MonthDay getMonthDay() {
        if (day == null || month == null) {
            return null;
        }
        try {
            return MonthDay.of(month, day);
        } catch (DateTimeException _) {
            return null;
        }
    }

    public List<CompanyClosureNameFormDto> getNames() {
        return names;
    }

    public void setNames(List<CompanyClosureNameFormDto> names) {
        this.names = names;
    }

    /**
     * @return the names by language, without the blank ones
     */
    public Map<Locale, String> getNameByLocale() {
        final Map<Locale, String> nameByLocale = new HashMap<>();
        for (CompanyClosureNameFormDto name : names) {
            if (name.getLocale() != null && hasText(name.getName())) {
                nameByLocale.put(name.getLocale(), name.getName());
            }
        }
        return nameByLocale;
    }

    /**
     * @return the name for the locale or its language (e.g. "de" for "de_DE"), the first given name otherwise
     */
    public String nameFor(Locale locale) {
        final Map<Locale, String> nameByLocale = getNameByLocale();
        return ofNullable(nameByLocale.get(locale))
            .or(() -> ofNullable(nameByLocale.get(forLanguageTag(locale.getLanguage()))))
            .or(() -> names.stream().map(CompanyClosureNameFormDto::getName).filter(StringUtils::hasText).findFirst())
            .orElse("");
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidFromIsoValue() {
        return validFrom == null ? "" : validFrom.format(DateTimeFormatter.ISO_DATE);
    }

    public List<CompanyClosureVersionFormDto> getVersions() {
        return versions;
    }

    public void setVersions(List<CompanyClosureVersionFormDto> versions) {
        this.versions = versions;
    }

    /**
     * @param currentDayLength the day length valid today, which the form starts with
     * @return whether a new version is entered, i.e. "valid from" is given or another day length is chosen
     */
    public boolean hasNewVersion(DayLength currentDayLength) {
        return validFrom != null || (dayLength != null && dayLength != currentDayLength);
    }
}
