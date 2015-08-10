package org.synyx.urlaubsverwaltung.core.department;

import java.util.Optional;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
public interface DepartmentService {

    /**
     * returns a department by given name.
     *
     * @param  name  of a department
     *
     * @return  Optional department
     */
    Optional<Department> getDepartmentByName(String name);


    /**
     * Saves the given department to repository.
     *
     * @param  department
     */
    void save(Department department);


    /**
     * Updates a given department in repository.
     *
     * @param  department
     */
    void update(Department department);
}
