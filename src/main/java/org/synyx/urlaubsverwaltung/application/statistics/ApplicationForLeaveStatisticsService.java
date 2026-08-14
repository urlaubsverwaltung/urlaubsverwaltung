package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.data.domain.Page;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

public interface ApplicationForLeaveStatisticsService {

    /**
     * Get {@link ApplicationForLeaveStatistics} the given person is allowed to see.
     * A person with {@link Role#BOSS} or {@link Role#OFFICE} is allowed to see statistics of everyone for instance.
     *
     * @param person             person to restrict the returned page content
     * @param period             filter result set for a given period of time
     * @return filtered page of {@link ApplicationForLeaveStatistics}
     */
    Page<ApplicationForLeaveStatistics> getStatisticsSortedByStatistics(Person person, FilterPeriod period);
}
