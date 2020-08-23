package org.synyx.urlaubsverwaltung.department.api;

import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.api.PersonsDto;
import org.synyx.urlaubsverwaltung.person.api.PersonDto;
import org.synyx.urlaubsverwaltung.person.api.PersonMapper;

import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.api.RestApiDateFormat.DATE_PATTERN;

class DepartmentDto {

    private String name;
    private String description;
    private String lastModification;
    private PersonsDto members;
    private PersonsDto departmentHeads;

    DepartmentDto(Department department) {

        this.name = department.getName();
        this.description = department.getDescription();
        this.lastModification = department.getLastModification().format(ofPattern(DATE_PATTERN));

        List<PersonDto> membersResponses = department.getMembers()
                .stream()
                .map(PersonMapper::mapToDto)
                .collect(Collectors.toList());

        this.members = new PersonsDto(membersResponses);

        List<PersonDto> departmentHeadsResponses = department.getDepartmentHeads()
                .stream()
                .map(PersonMapper::mapToDto)
                .collect(Collectors.toList());

        this.departmentHeads = new PersonsDto(departmentHeadsResponses);
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

    public PersonsDto getMembers() {
        return members;
    }

    public void setMembers(PersonsDto members) {
        this.members = members;
    }

    public PersonsDto getDepartmentHeads() {
        return departmentHeads;
    }

    public void setDepartmentHeads(PersonsDto departmentHeads) {
        this.departmentHeads = departmentHeads;
    }
}
