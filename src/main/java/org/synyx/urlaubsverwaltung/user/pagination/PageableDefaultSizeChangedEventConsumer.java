package org.synyx.urlaubsverwaltung.user.pagination;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.web.pageable.PageableDefaultSizeChangedEvent;

@Component
class PageableDefaultSizeChangedEventConsumer {

    private final UserPaginationSettingsService userPaginationSettingsService;

    PageableDefaultSizeChangedEventConsumer(UserPaginationSettingsService userPaginationSettingsService) {
        this.userPaginationSettingsService = userPaginationSettingsService;
    }

    @Async
    @EventListener
    public void onPageableDefaultSizeChanged(PageableDefaultSizeChangedEvent event) {
        userPaginationSettingsService.updatePageableDefaultSize(event.personId(), event.newPageableDefaultSize());
    }
}
