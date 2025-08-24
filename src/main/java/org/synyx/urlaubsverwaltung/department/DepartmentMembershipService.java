package org.synyx.urlaubsverwaltung.department;

import java.util.List;

public interface DepartmentMembershipService {

    /**
     * Get all {@link DepartmentMembership}.
     *
     * @return all department memberships
     */
    List<DepartmentMembership> getAllDepartmentMemberships();
}
