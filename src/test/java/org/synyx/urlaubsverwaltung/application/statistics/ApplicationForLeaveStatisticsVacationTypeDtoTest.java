package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ApplicationForLeaveStatisticsVacationTypeDtoTest {

    @Test
    void ensureEquals() {
        ApplicationForLeaveStatisticsVacationTypeDto dto1 = new ApplicationForLeaveStatisticsVacationTypeDto("label", 1L);
        ApplicationForLeaveStatisticsVacationTypeDto dto2 = new ApplicationForLeaveStatisticsVacationTypeDto("label", 1L);
        ApplicationForLeaveStatisticsVacationTypeDto dto3 = new ApplicationForLeaveStatisticsVacationTypeDto("other label", 1L);
        ApplicationForLeaveStatisticsVacationTypeDto dto4 = new ApplicationForLeaveStatisticsVacationTypeDto("label", 2L);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1, dto4);
    }
}

