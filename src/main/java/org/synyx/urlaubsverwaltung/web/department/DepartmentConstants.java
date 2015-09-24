
package org.synyx.urlaubsverwaltung.web.department;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants concerning {@link DepartmentController}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DepartmentConstants {

    // JSPs
    public static final String DEPARTMENT_JSP = "department/department_list";
    public static final String DEPARTMENT_FORM_JSP = "department/department_form";

    // Attributes
    public static final String DEPARTMENTS_ATTRIBUTE = "departments";
    public static final String DEPARTMENT_ATTRIBUTE = "department";
}
