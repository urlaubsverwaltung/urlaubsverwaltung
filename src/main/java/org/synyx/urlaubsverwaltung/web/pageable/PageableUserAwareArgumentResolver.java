package org.synyx.urlaubsverwaltung.web.pageable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
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

import static java.lang.Integer.parseInt;

/**
 * Extends springs default {@linkplain PageableHandlerMethodArgumentResolver} with {@linkplain UserPaginationSettings}
 * as fallback values.
 */
class PageableUserAwareArgumentResolver extends PageableHandlerMethodArgumentResolver {

    private final UserPaginationSettingsSupplier userPaginationSettingsSupplier;
    private final PersonService personService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DataWebProperties dataWebProperties;

    PageableUserAwareArgumentResolver(
        SortHandlerMethodArgumentResolver sortHandlerMethodArgumentResolver,
        UserPaginationSettingsSupplier userPaginationSettingsSupplier,
        PersonService personService,
        ApplicationEventPublisher applicationEventPublisher,
        DataWebProperties dataWebProperties
    ) {
        super(sortHandlerMethodArgumentResolver);
        this.userPaginationSettingsSupplier = userPaginationSettingsSupplier;
        this.personService = personService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.dataWebProperties = dataWebProperties;
    }

    @NonNull
    @Override
    public Pageable resolveArgument(@NonNull MethodParameter methodParameter, @Nullable ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {

        final Person signedInUser = personService.getSignedInUser();
        final PersonId signedInUserId = signedInUser.getIdAsPersonId();

        final Pageable pageableResolved = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        final String pageSizeParameter = webRequest.getParameter(getParameterNameToUse(getSizeParameterName(), methodParameter));

        // page index can be configured for the web layer.
        // however, calculation and spring are expecting 0-based page numbers.
        // therefore, we're mapping this here as the layer between web and services.
        final Pageable pageable;
        if (dataWebProperties.getPageable().isOneIndexedParameters()) {
            final int pageNumber = Math.max(pageableResolved.getPageNumber() - 1, 0);
            pageable = PageRequest.of(pageNumber, pageableResolved.getPageSize(), pageableResolved.getSort());
        } else {
            pageable = pageableResolved;
        }

        if (pageSizeParameter == null) {
            // note that this overrides possible @PageableDefault(size) annotations.
            final int defaultPageSize = userPaginationSettingsSupplier
                .getUserPaginationSettings(signedInUserId)
                .getDefaultPageSize();
            return PageRequest.of(pageable.getPageNumber(), defaultPageSize, pageable.getSort());
        } else {
            applicationEventPublisher.publishEvent(new PageableDefaultSizeChangedEvent(signedInUserId, parseInt(pageSizeParameter)));
        }

        return pageable;
    }
}
