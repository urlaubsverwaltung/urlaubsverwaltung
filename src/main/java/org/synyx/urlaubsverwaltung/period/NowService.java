package org.synyx.urlaubsverwaltung.period;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;


/**
 * Provides better mockability of current date and time in tests.
 */
@Service
public class NowService {

    public LocalDate now() {

        return LocalDate.now(UTC);
    }


    public int currentYear() {

        return ZonedDateTime.now(UTC).getYear();
    }
}
