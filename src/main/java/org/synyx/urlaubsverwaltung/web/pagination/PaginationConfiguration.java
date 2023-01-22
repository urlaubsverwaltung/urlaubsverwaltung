package org.synyx.urlaubsverwaltung.web.pagination;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsSupplier;

@Configuration
public class PaginationConfiguration extends SpringDataWebConfiguration {

    public PaginationConfiguration(ApplicationContext context,
                                   @Qualifier("mvcConversionService") ObjectFactory<ConversionService> conversionService) {

        super(context, conversionService);
    }

    @Bean
    public PageableHandlerMethodArgumentResolver pageableResolver(SortHandlerMethodArgumentResolver sortResolver,
                                                                  UserPaginationSettingsSupplier userPaginationSettingsSupplier,
                                                                  PersonService personService,
                                                                  ApplicationEventPublisher applicationEventPublisher) {

        PageableHandlerMethodArgumentResolver pageableResolver =
            new PageableUserAwareArgumentResolver(sortResolver, userPaginationSettingsSupplier, personService, applicationEventPublisher);

        customizePageableResolver(pageableResolver);

        return pageableResolver;
    }
}
