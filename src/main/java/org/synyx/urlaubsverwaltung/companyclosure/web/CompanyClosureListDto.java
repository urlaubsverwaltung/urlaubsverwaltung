package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersion;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.MonthDay;
import java.util.List;

/**
 * @param id                month day as used in the url, e.g. {@code 12-24}
 * @param currentDayLength  closed portion of the day valid today
 */
public record CompanyClosureListDto(String id, MonthDay monthDay, String name, DayLength currentDayLength,
                                    List<CompanyClosureVersion> versions) {
}
