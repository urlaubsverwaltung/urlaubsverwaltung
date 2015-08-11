package org.synyx.urlaubsverwaltung.core.department;

import org.junit.Test;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
public class DepartmentTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureNotNullDateTime() throws Exception {

        new Department().setLastModification(null);
    }
}
