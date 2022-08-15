package org.synyx.urlaubsverwaltung.overtime;

import java.time.Duration;

public class LeftOvertimeOverall {

    private final Duration leftOvertime;

    public LeftOvertimeOverall(Duration leftOvertime) {
        this.leftOvertime = leftOvertime;
    }

    public Duration getLeftOvertime() {
        return leftOvertime;
    }
}
