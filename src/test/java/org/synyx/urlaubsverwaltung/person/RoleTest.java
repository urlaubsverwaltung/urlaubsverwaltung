package org.synyx.urlaubsverwaltung.person;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

class RoleTest {

    @Test
    void privilegedRoles() {
        assertThat(Role.privilegedRoles()).containsAll(List.of(DEPARTMENT_HEAD, BOSS, OFFICE, SECOND_STAGE_AUTHORITY));
    }
}
