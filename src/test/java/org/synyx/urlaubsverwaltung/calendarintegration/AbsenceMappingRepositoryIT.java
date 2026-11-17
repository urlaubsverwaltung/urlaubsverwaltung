package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.VACATION;

/**
 * Deliberately not {@code @Transactional}: the calendar sync deletes mappings from an async listener without a
 * transaction of its own.
 */
@SpringBootTest
class AbsenceMappingRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private AbsenceMappingRepository sut;

    @AfterEach
    void tearDown() {
        sut.deleteAll();
    }

    @Test
    void ensureDeleteByEventIdWorksWithoutTransactionOfTheCaller() {

        sut.save(new AbsenceMapping(1L, VACATION, "eventId"));
        sut.save(new AbsenceMapping(2L, VACATION, "otherEventId"));

        sut.deleteByEventId("eventId");

        assertThat(sut.findAll()).extracting(AbsenceMapping::getEventId).containsExactly("otherEventId");
    }
}
