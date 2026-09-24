package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnknownBlackoutPeriodExceptionTest {

    @Test
    void ensureMessageContainsTheUnknownId() {
        final UnknownBlackoutPeriodException exception = new UnknownBlackoutPeriodException(42L);
        assertThat(exception).hasMessage("No blackoutperiod found for ID = 42");
    }
}
