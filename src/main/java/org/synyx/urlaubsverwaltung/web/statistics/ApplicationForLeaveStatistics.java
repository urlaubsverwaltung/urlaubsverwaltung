package org.synyx.urlaubsverwaltung.web.statistics;

import lombok.Data;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;

import java.math.BigDecimal;


/**
 * Encapsulates information about a person and the corresponding vacation days information (how many applications for
 * leave are waiting, how many are allowed, how many vacation days has the person left for using).
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Data
public class ApplicationForLeaveStatistics {

    private final Person person;
    private final String gravatarUrl;

    private BigDecimal waitingVacationDays = BigDecimal.ZERO;
    private BigDecimal allowedVacationDays = BigDecimal.ZERO;
    private BigDecimal leftVacationDays = BigDecimal.ZERO;

    public ApplicationForLeaveStatistics(Person person) {

        this.person = person;
        this.gravatarUrl = GravatarUtil.createImgURL(person.getEmail());
    }

}
