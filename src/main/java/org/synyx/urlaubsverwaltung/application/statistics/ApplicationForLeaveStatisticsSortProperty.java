package org.synyx.urlaubsverwaltung.application.statistics;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonSortProperty;

import java.util.Optional;
import java.util.function.Function;

public enum ApplicationForLeaveStatisticsSortProperty {

    TOTAL_ALLOWED_VACATION_DAYS(ApplicationForLeaveStatisticsSortProperty.TOTAL_ALLOWED_VACATION_DAYS_KEY, ApplicationForLeaveStatistics::getTotalAllowedVacationDays),
    TOTAL_WAITING_VACATION_DAYS(ApplicationForLeaveStatisticsSortProperty.TOTAL_WAITING_VACATION_DAYS_KEY, ApplicationForLeaveStatistics::getTotalWaitingVacationDays),
    LEFT_VACATION_DAYS_FOR_PERIOD(ApplicationForLeaveStatisticsSortProperty.LEFT_VACATION_DAYS_FOR_PERIOD_KEY, ApplicationForLeaveStatistics::getLeftVacationDaysForPeriod),
    LEFT_VACATION_DAYS_FOR_YEAR(ApplicationForLeaveStatisticsSortProperty.LEFT_VACATION_DAYS_FOR_YEAR_KEY, ApplicationForLeaveStatistics::getLeftVacationDaysForYear);

    /**
     * Value used by APIs, in URLQueryParams for instance.
     *
     * <p>
     * This is not bound to {@link ApplicationForLeaveStatistics} attribute names, {@link ApplicationForLeaveStatisticsSortProperty#propertyExtractor()} is!
     */
    private final String key;

    /**
     * a {@link ApplicationForLeaveStatisticsSortProperty} function (e.g. <code>ApplicationForLeaveStatisticsSortProperty::getLeftVacationDaysForPeriod</code>)
     */
    private final Function<ApplicationForLeaveStatistics, ?> propertyExtractor;

    public static final String TOTAL_ALLOWED_VACATION_DAYS_KEY = "totalAllowedVacationDays";
    public static final String TOTAL_WAITING_VACATION_DAYS_KEY = "totalWaitingVacationDays";
    public static final String LEFT_VACATION_DAYS_FOR_PERIOD_KEY = "leftVacationDaysForPeriod";
    public static final String LEFT_VACATION_DAYS_FOR_YEAR_KEY = "leftVacationDaysForYear";

    ApplicationForLeaveStatisticsSortProperty(String key, Function<ApplicationForLeaveStatistics, ?> propertyExtractor) {
        this.key = key;
        this.propertyExtractor = propertyExtractor;
    }

    /**
     * Creates the {@link ApplicationForLeaveStatisticsSortProperty} for the given key.
     *
     * @param key key to map
     * @return the matched {@link ApplicationForLeaveStatisticsSortProperty}, {@link Optional#empty()} if key is unknown.
     */
    public static Optional<ApplicationForLeaveStatisticsSortProperty> byKey(String key) {
        final ApplicationForLeaveStatisticsSortProperty sort = switch (key) {
            case TOTAL_ALLOWED_VACATION_DAYS_KEY -> TOTAL_ALLOWED_VACATION_DAYS;
            case TOTAL_WAITING_VACATION_DAYS_KEY -> TOTAL_WAITING_VACATION_DAYS;
            case LEFT_VACATION_DAYS_FOR_PERIOD_KEY -> LEFT_VACATION_DAYS_FOR_PERIOD;
            case LEFT_VACATION_DAYS_FOR_YEAR_KEY -> LEFT_VACATION_DAYS_FOR_YEAR;
            default -> null;
        };
        return Optional.ofNullable(sort);
    }

    /**
     * Can be used to expose this property as string.
     *
     * @return a string representation of this {@link PersonSortProperty}
     */
    public String key() {
        return key;
    }

    /**
     * The {@link ApplicationForLeaveStatistics} property extractor of this {@link ApplicationForLeaveStatisticsSortProperty}.
     *
     * <p>
     * Usage:
     *
     * <pre><code>
     *     Sort.TypedSort<ApplicationForLeaveStatistics> typeSort = Sort.sort(ApplicationForLeaveStatistics.class);
     *     typeSort.by(TOTAL_ALLOWED_VACATION_DAYS::propertyExtractor);
     * </code></pre>
     *
     * @return the {@link Person} property extractor
     */
    public Function<ApplicationForLeaveStatistics, ?> propertyExtractor() {
        return propertyExtractor;
    }
}
