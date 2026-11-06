package org.synyx.urlaubsverwaltung.department.web;

import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

/**
 * Model of the department picker fragment ({@code fragments/department-picker}).
 *
 * @param options            one option per department the signed-in person may choose
 * @param allPersonsSelected whether "all persons" is chosen instead of departments
 * @param allPersonsCount    number of persons shown when "all persons" is chosen
 */
public record DepartmentPickerDto(List<Option> options, boolean allPersonsSelected, int allPersonsCount) {

    // no description of the department: we do not know whether it is meant for everyone who can choose the
    // department. at the moment only boss and office see it, in the department list.
    public record Option(String name, int activeMemberCount, boolean selected) {
    }

    public static DepartmentPickerDto of(List<Department> departments, List<String> selectedDepartmentNames,
                                         boolean allPersonsSelected, int allPersonsCount) {
        final List<Option> options = departments.stream()
            .map(department -> new Option(
                department.getName(),
                (int) department.getMembers().stream().filter(Person::isActive).count(),
                selectedDepartmentNames.contains(department.getName())
            ))
            .toList();
        return new DepartmentPickerDto(options, allPersonsSelected, allPersonsCount);
    }

    public List<String> selectedNames() {
        return options.stream()
            .filter(Option::selected)
            .map(Option::name)
            .toList();
    }
}
