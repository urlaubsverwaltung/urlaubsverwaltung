package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;

class ApplicationStatusTest {

    @Test
    void ensureActiveApplicationStatusAreCorrectlyDefined() {
        assertThat(ApplicationStatus.activeStatuses())
            .containsExactly(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
    }

    @Test
    void ensureInactiveApplicationStatusAreCorrectlyDefined() {
        assertThat(ApplicationStatus.inactiveStatuses())
            .containsExactly(REVOKED, REJECTED, CANCELLED);
    }

    @Test
    void ensureThatEveryStatusWillBePartOfActiveOrInactive() {
        final List<ApplicationStatus> combinedList = concat(ApplicationStatus.inactiveStatuses().stream(), ApplicationStatus.activeStatuses().stream()).toList();
        assertThat(ApplicationStatus.values()).hasSameElementsAs(combinedList);
    }
}
