package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.Arrays;
import java.util.Collection;

public class SecurityProviderTest {

    private SecurityProvider securityProvider;

    @Before
    public void setUp() {
        securityProvider = new SecurityProvider();
    }

    @Test
    public void ensureReturnsTrueIfSignedInUserHasSameIdAsRequestedDataFor() {

        Authentication authentication = Mockito.mock(Authentication.class);
        CustomPrincipal principal = Mockito.mock(CustomPrincipal.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(principal);
        Mockito.when(principal.getId()).thenReturn(1);

        Assert.assertTrue("Should be true if requesting own data", securityProvider.loggedInUserRequestsOwnData(authentication, 1));
    }

    @Test
    public void ensureReturnsFalseIfSignedInUserHasOtherIdThanRequestedDataFor() {

        Authentication authentication = Mockito.mock(Authentication.class);
        CustomPrincipal principal = Mockito.mock(CustomPrincipal.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(principal);
        Mockito.when(principal.getId()).thenReturn(2);

        Assert.assertFalse("Should be false if requesting data of someone else", securityProvider.loggedInUserRequestsOwnData(authentication, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsExceptionIfGivenIdIsNull() {

        securityProvider.loggedInUserRequestsOwnData(Mockito.mock(Authentication.class), null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsExceptionIfGivenAuthenticationIsNull() {

        securityProvider.loggedInUserRequestsOwnData(null, 1);

    }
}
