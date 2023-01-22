package org.synyx.urlaubsverwaltung.web.pageable;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettings;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsSupplier;

/**
 * Extends springs default {@linkplain PageableHandlerMethodArgumentResolver} with {@linkplain UserPaginationSettings}
 * as fallback values.
 */
class PageableUserAwareArgumentResolver extends PageableHandlerMethodArgumentResolver {

    private final UserPaginationSettingsSupplier userPaginationSettingsSupplier;
    private final PersonService personService;
    private final ApplicationEventPublisher applicationEventPublisher;

    PageableUserAwareArgumentResolver(SortHandlerMethodArgumentResolver sortHandlerMethodArgumentResolver,
                                      UserPaginationSettingsSupplier userPaginationSettingsSupplier,
                                      PersonService personService, ApplicationEventPublisher applicationEventPublisher) {

        super(sortHandlerMethodArgumentResolver);
        this.userPaginationSettingsSupplier = userPaginationSettingsSupplier;
        this.personService = personService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return super.supportsParameter(methodParameter);
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        final Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        final Person signedInUser = personService.getSignedInUser();
        final UserPaginationSettings userPaginationSettings = userPaginationSettingsSupplier.getUserPaginationSettings(new PersonId(signedInUser.getId()));
        final String pageSizeParameter = webRequest.getParameter(getParameterNameToUse(getSizeParameterName(), methodParameter));

        if (pageSizeParameter == null) {
            // note that this overrides possible @PageableDefault(size) annotations.
            final int defaultPageSize = userPaginationSettings.getDefaultPageSize();
            return PageRequest.of(pageable.getPageNumber(), defaultPageSize, pageable.getSort());
        } else {
            final int newValue = Integer.parseInt(pageSizeParameter);
            applicationEventPublisher.publishEvent(new PageableDefaultSizeChangedEvent(new PersonId(signedInUser.getId()), newValue));
        }

        return pageable;
    }
}
