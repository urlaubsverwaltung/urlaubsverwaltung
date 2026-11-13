package org.synyx.urlaubsverwaltung.web.pageable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettings;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageableUserAwareArgumentResolverTest {

    private PageableUserAwareArgumentResolver sut;

    @Mock
    private UserPaginationSettingsSupplier userPaginationSettingsSupplier;
    @Mock
    private PersonService personService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private final PersonId personId = new PersonId(1L);

    @BeforeEach
    void setUp() {
        final DataWebProperties dataWebProperties = new DataWebProperties();
        dataWebProperties.getPageable().setOneIndexedParameters(true);

        sut = new PageableUserAwareArgumentResolver(new SortHandlerMethodArgumentResolver(),
            userPaginationSettingsSupplier, personService, applicationEventPublisher, dataWebProperties);

        final Person signedInUser = new Person();
        signedInUser.setId(personId.value());
        when(personService.getSignedInUser()).thenReturn(signedInUser);
    }

    @Test
    void ensureToRememberTheRequestedPageSize() throws Exception {

        final Pageable pageable = resolve("50");

        assertThat(pageable.getPageSize()).isEqualTo(50);
        verify(applicationEventPublisher).publishEvent(new PageableDefaultSizeChangedEvent(personId, 50));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "", "1000000"})
    void ensureToUseTheUsersPageSizeWithoutRememberingAnInvalidOne(String size) throws Exception {

        when(userPaginationSettingsSupplier.getUserPaginationSettings(personId))
            .thenReturn(new UserPaginationSettings.Builder().defaultPageSize(50).build());

        final Pageable pageable = resolve(size);

        assertThat(pageable.getPageSize()).isEqualTo(50);
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    private Pageable resolve(String size) throws NoSuchMethodException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("size", size);
        final MethodParameter parameter = new MethodParameter(getClass().getDeclaredMethod("handler", Pageable.class), 0);
        return sut.resolveArgument(parameter, null, new ServletWebRequest(request), null);
    }

    @SuppressWarnings("unused")
    private void handler(Pageable pageable) {
        // only its parameter is resolved
    }
}
