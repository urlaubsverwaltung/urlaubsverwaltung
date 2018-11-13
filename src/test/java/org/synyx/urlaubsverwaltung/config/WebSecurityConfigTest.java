package org.synyx.urlaubsverwaltung.config;

import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;

import org.springframework.core.env.Environment;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import org.synyx.urlaubsverwaltung.security.SimpleAuthenticationProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static java.util.Optional.empty;


/**
 * @author  Ben Antony - antony@synyx.de
 */
public class WebSecurityConfigTest {

    private WebSecurityConfig sut;

    private Environment environmentMock;
    private AuthenticationManagerBuilder authBuilderMock;
    private ArgumentCaptor<AuthenticationProvider> authenticationProviderCaptor;

    @Before
    public void setUp() {

        environmentMock = mock(Environment.class);
        authBuilderMock = mock(AuthenticationManagerBuilder.class);
        authenticationProviderCaptor = ArgumentCaptor.forClass(AuthenticationProvider.class);
    }


    @Test
    public void configureAuthDefault() throws Exception {

        SimpleAuthenticationProvider simpleAuthenticationProviderMock = mock(SimpleAuthenticationProvider.class);
        Optional<SimpleAuthenticationProvider> optionalSimpleAuthProvider = Optional.of(
                simpleAuthenticationProviderMock);
        sut = new WebSecurityConfig(environmentMock, optionalSimpleAuthProvider, empty());

        when(environmentMock.getProperty("auth")).thenReturn("default");

        sut.configure(authBuilderMock);

        verify(authBuilderMock).authenticationProvider(authenticationProviderCaptor.capture());

        assertThat(authenticationProviderCaptor.getValue()).isEqualTo(simpleAuthenticationProviderMock);

        verifyNoMoreInteractions(authBuilderMock);
    }


    @Test
    public void configureAuthActiveDirectory() throws Exception {

        SimpleAuthenticationProvider simpleAuthenticationProviderMock = mock(SimpleAuthenticationProvider.class);
        Optional<SimpleAuthenticationProvider> optionalSimpleAuthProvider = Optional.of(
                simpleAuthenticationProviderMock);

        Optional<UserDetailsContextMapper> personContextMapperMock = Optional.of(mock(UserDetailsContextMapper.class));

        sut = new WebSecurityConfig(environmentMock, optionalSimpleAuthProvider, personContextMapperMock);

        when(environmentMock.getProperty("auth")).thenReturn("activeDirectory");
        when(environmentMock.getProperty("uv.security.activeDirectory.url")).thenReturn("http://domain.tld");

        sut.configure(authBuilderMock);

        verify(authBuilderMock).authenticationProvider(authenticationProviderCaptor.capture());

        assertThat(authenticationProviderCaptor.getValue()).isOfAnyClassIn(
            ActiveDirectoryLdapAuthenticationProvider.class);

        verifyNoMoreInteractions(authBuilderMock);
    }
}
