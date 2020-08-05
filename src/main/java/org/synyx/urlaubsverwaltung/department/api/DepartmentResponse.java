package org.synyx.urlaubsverwaltung.department.api;

import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.api.PersonListResponse;
import org.synyx.urlaubsverwaltung.person.api.PersonResponse;
import org.synyx.urlaubsverwaltung.person.api.PersonResponseMapper;

import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.api.RestApiDateFormat.DATE_PATTERN;


class DepartmentResponse {

    private String name;
    private String description;
    private String lastModification;
    private PersonListResponse members;
    private PersonListResponse departmentHeads;

    DepartmentResponse(Department department) {

        this.name = department.getName();
        this.description = department.getDescription();
        this.lastModification = department.getLastModification().format(ofPattern(DATE_PATTERN));

        List<PersonResponse> membersResponses = department.getMembers()
                .stream()
                .map(PersonResponseMapper::mapToResponse)
                .collect(Collectors.toList());

        this.members = new PersonListResponse(membersResponses);

        List<PersonResponse> departmentHeadsResponses = department.getDepartmentHeads()
                .stream()
                .map(PersonResponseMapper::mapToResponse)
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
