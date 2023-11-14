package org.synyx.urlaubsverwaltung.department.api;

import org.springframework.hateoas.RepresentationModel;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.api.PersonDto;
import org.synyx.urlaubsverwaltung.person.api.PersonMapper;
import org.synyx.urlaubsverwaltung.person.api.PersonsDto;

import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.api.RestApiDateFormat.DATE_PATTERN;

public class DepartmentDto extends RepresentationModel<DepartmentDto> {

    private final String name;
    private final String description;
    private final String lastModification;
    private final PersonsDto members;
    private final PersonsDto departmentHeads;

    DepartmentDto(Department department) {

        this.name = department.getName();
        this.description = department.getDescription();
        this.lastModification = department.getLastModification().format(ofPattern(DATE_PATTERN));

        final List<PersonDto> membersResponses = department.getMembers()
            .stream()
            .map(PersonMapper::mapToDto)
            .toList();

        this.members = new PersonsDto(membersResponses);

        final List<PersonDto> departmentHeadsResponses = department.getDepartmentHeads()
            .stream()
            .map(PersonMapper::mapToDto)
            .toList();

        this.departmentHeads = new PersonsDto(departmentHeadsResponses);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLastModification() {
        return lastModification;
    }

    public PersonsDto getMembers() {
        return members;
    }

    public PersonsDto getDepartmentHeads() {
        return departmentHeads;
    }
}
