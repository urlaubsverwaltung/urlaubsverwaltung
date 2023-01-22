package org.synyx.urlaubsverwaltung.user.pagination;

import org.synyx.urlaubsverwaltung.person.PersonId;

public interface UserPaginationSettingsSupplier {

    UserPaginationSettings getUserPaginationSettings(PersonId personId);
}
