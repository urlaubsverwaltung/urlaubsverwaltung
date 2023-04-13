package org.synyx.urlaubsverwaltung.user.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableAsync;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.web.pageable.PageableDefaultSizeChangedEvent;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(classes = { PageableDefaultSizeChangedEventConsumer.class, UserPaginationSettingsService.class })
@EnableAsync
class PageableDefaultSizeChangedEventConsumerIT {

    @Autowired
    private PageableDefaultSizeChangedEventConsumer sut;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private UserPaginationSettingsService userPaginationSettingsService;

    @Test
    void ensurePageableDefaultSizeChangedEventUpdatesPageableDefaultSizeOfUser() {

        final PageableDefaultSizeChangedEvent event = new PageableDefaultSizeChangedEvent(new PersonId(1), 9001);
        eventPublisher.publishEvent(event);

        verifyNoInteractions(userPaginationSettingsService);

        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            verify(userPaginationSettingsService).updatePageableDefaultSize(new PersonId(1), 9001);
        });
    }
}
