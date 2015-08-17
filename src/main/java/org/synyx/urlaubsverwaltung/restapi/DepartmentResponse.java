package org.synyx.urlaubsverwaltung.restapi;

import org.synyx.urlaubsverwaltung.core.department.Department;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
public class DepartmentResponse {

    private String name;
    private String description;
    private String lastModification;
    private PersonListResponse members;
    private PersonListResponse departmentHeads;

    public DepartmentResponse(Department department) {

        this.name = department.getName();
        this.description = department.getName();
        this.lastModification = department.getLastModification().toString(RestApiDateFormat.PATTERN);

        List<PersonResponse> membersResponses = department.getMembers()
            .stream()
            .map(PersonResponse::new)
            .collect(Collectors.toList());

        this.members = new PersonListResponse(membersResponses);

        List<PersonResponse> departmentHeadsResponses = department.getDepartmentHeads()
            .stream()
            .map(PersonResponse::new)
            .collect(Collectors.toList());

        this.departmentHeads = new PersonListResponse(departmentHeadsResponses);
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


    public PersonListResponse getMembers() {

        return members;
    }


    public void setMembers(PersonListResponse members) {

        this.members = members;
    }


    public PersonListResponse getDepartmentHeads() {

        return departmentHeads;
    }


    public void setDepartmentHeads(PersonListResponse departmentHeads) {

        this.departmentHeads = departmentHeads;
    }
}
