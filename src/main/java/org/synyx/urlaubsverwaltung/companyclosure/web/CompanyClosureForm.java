package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

/**
 * Day and month are only used to create a closure day, an existing one is identified by the url.
 */
public class CompanyClosureForm {

    private Integer day;
    private Integer month;
    /**
     * one name per supported language in the order of {@link org.synyx.urlaubsverwaltung.settings.SupportedLanguages}
     */
    private List<CompanyClosureNameForm> names = new ArrayList<>();
    private DayLength dayLength;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate validFrom;

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

    public List<CompanyClosureNameForm> getNames() {
        return names;
    }

    public void setNames(List<CompanyClosureNameForm> names) {
        this.names = names;
    }

    /**
     * @return the names by language, without the blank ones
     */
    public Map<Locale, String> getNameByLocale() {
        final Map<Locale, String> nameByLocale = new HashMap<>();
        for (CompanyClosureNameForm name : names) {
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
        return Optional.ofNullable(nameByLocale.get(locale))
            .or(() -> Optional.ofNullable(nameByLocale.get(Locale.forLanguageTag(locale.getLanguage()))))
            .or(() -> names.stream().map(CompanyClosureNameForm::getName).filter(StringUtils::hasText).findFirst())
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
}
