package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class LeftOvertimeTest {

    @Test
    void ensureIdentity() {

        final LeftOvertime identity = LeftOvertime.identity();

        assertThat(identity.leftOvertimeOverall()).isEqualTo(Duration.ZERO);
        assertThat(identity.leftOvertimeDateRange()).isEqualTo(Duration.ZERO);
    }
}
