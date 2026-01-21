package org.synyx.urlaubsverwaltung.web.pageable;

import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsSupplier;

import java.util.List;

@Configuration
class PageableConfiguration implements WebMvcConfigurer {

    private final SortHandlerMethodArgumentResolver sortResolver;
    private final UserPaginationSettingsSupplier userPaginationSettingsSupplier;
    private final PersonService personService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DataWebProperties dataWebProperties;

    PageableConfiguration(
        SortHandlerMethodArgumentResolver sortResolver,
        UserPaginationSettingsSupplier userPaginationSettingsSupplier,
        PersonService personService,
        ApplicationEventPublisher applicationEventPublisher,
        DataWebProperties dataWebProperties
    ) {
        this.sortResolver = sortResolver;
        this.userPaginationSettingsSupplier = userPaginationSettingsSupplier;
        this.personService = personService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.dataWebProperties = dataWebProperties;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new PageableUserAwareArgumentResolver(
            sortResolver, userPaginationSettingsSupplier, personService, applicationEventPublisher, dataWebProperties
        ));
    }
}
