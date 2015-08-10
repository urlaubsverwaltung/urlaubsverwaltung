package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * Time configuration for half day absences (start and end time for morning resp. noon).
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
@Component
public class AbsenceTimeConfiguration {

    private Integer morningStart;
    private Integer morningEnd;
    private Integer noonStart;
    private Integer noonEnd;

    @Autowired
    public AbsenceTimeConfiguration(@Value("${calendar.time.morningStart}") Integer morningStart,
        @Value("${calendar.time.morningEnd}") Integer morningEnd,
        @Value("${calendar.time.noonStart}") Integer noonStart,
        @Value("${calendar.time.noonEnd}") Integer noonEnd) {

        this.morningStart = morningStart;
        this.morningEnd = morningEnd;
        this.noonStart = noonStart;
        this.noonEnd = noonEnd;
    }

    public long getMorningStart() {

        return TimeUnit.HOURS.toMillis(morningStart);
    }


    public long getMorningEnd() {

        return TimeUnit.HOURS.toMillis(morningEnd);
    }


    public long getNoonStart() {

        return TimeUnit.HOURS.toMillis(noonStart);
    }


    public long getNoonEnd() {

        return TimeUnit.HOURS.toMillis(noonEnd);
    }
}
