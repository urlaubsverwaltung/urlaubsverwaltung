package org.synyx.urlaubsverwaltung.user.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.web.pageable.PageableDefaultSizeChangedEvent;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {PageableDefaultSizeChangedEventConsumer.class, UserPaginationSettingsService.class})
class PageableDefaultSizeChangedEventConsumerIT {

    @Autowired
    private PageableDefaultSizeChangedEventConsumer sut;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private UserPaginationSettingsService userPaginationSettingsService;

    @Test
    void ensurePageableDefaultSizeChangedEventUpdatesPageableDefaultSizeOfUser() {

        final PageableDefaultSizeChangedEvent event = new PageableDefaultSizeChangedEvent(new PersonId(1L), 9001);
        eventPublisher.publishEvent(event);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> verify(userPaginationSettingsService).updatePageableDefaultSize(new PersonId(1L), 9001));
    }
}
