package org.synyx.urlaubsverwaltung.core.period;

import org.joda.time.DateMidnight;
import org.springframework.stereotype.Service;


/**
 * Provides better mockability of current date and time in tests.
 */
@Service
public class NowService {

    public DateMidnight now() {

        return DateMidnight.now();
    }


    public int currentYear() {

        return now().getYear();
    }
}
