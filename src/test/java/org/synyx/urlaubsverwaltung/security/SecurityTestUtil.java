package org.synyx.urlaubsverwaltung.security;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;


/**
 * Helper class for tests concerning security relevant stuff.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SecurityTestUtil {

    public static boolean authorityForRoleExists(Collection<? extends GrantedAuthority> authorities, final Role role) {

        Optional<? extends GrantedAuthority> authorityForRoleExistsOptional = Iterables.tryFind(authorities,
                new Predicate<GrantedAuthority>() {

                    @Override
                    public boolean apply(GrantedAuthority input) {

                        if (input.getAuthority().equals(role.name())) {
                            return true;
                        }

                        return false;
                    }
                });

        return authorityForRoleExistsOptional.isPresent();
    }
}
