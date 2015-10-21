package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapSyncServiceTest {

    private LdapUserService ldapUserService;

    private LdapSyncService ldapSyncService;

    @Before
    public void setUp() {

        ldapUserService = Mockito.mock(LdapUserService.class);

        ldapSyncService = new LdapSyncService(ldapUserService);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfSyncIsCalledAndNoAuthenticationSet() {

        System.getProperties().remove(Authentication.PROPERTY_KEY);

        ldapSyncService.sync();
    }


    @Test
    public void ensureThrowsIfSyncIsCalledAndAuthenticationTypeIsNotLdap() {

        Consumer<String> assertThrows = (auth) -> {
            System.getProperties().put(Authentication.PROPERTY_KEY, auth);

            try {
                ldapSyncService.sync();
                Assert.fail("Sync should throw for " + Authentication.PROPERTY_KEY + "=" + auth);
            } catch (IllegalStateException ex) {
                // Expected
            }
        };

        assertThrows.accept(Authentication.Type.DEFAULT.getName());
        assertThrows.accept(Authentication.Type.ACTIVE_DIRECTORY.getName());
        assertThrows.accept("foo");
    }


    @Test
    public void ensureFetchesLdapUsers() {

        System.getProperties().put(Authentication.PROPERTY_KEY, Authentication.Type.LDAP.getName());

        ldapSyncService.sync();

        Mockito.verify(ldapUserService).getLdapUsers();
    }
}
