package org.synyx.urlaubsverwaltung.restapi;

import org.synyx.urlaubsverwaltung.core.department.Department;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
public class DepartmentResponse {

    private String name;
    private String description;
    private String lastModification;

    public DepartmentResponse(Department department) {

        this.name = department.getName();
        this.description = department.getName();
        this.lastModification = department.getLastModification().toString(RestApiDateFormat.PATTERN);
    }

    public String getName() {

        return name;
    }


    public void setName(String name) {

        this.name = name;
    }


    public String getDescription() {

        return description;
    }


    public void setDescription(String description) {

        this.description = description;
    }


    public String getLastModification() {

        return lastModification;
    }


    public void setLastModification(String lastModification) {

        this.lastModification = lastModification;
    }
}
