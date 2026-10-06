package org.synyx.urlaubsverwaltung.department.web;

import java.util.List;

/**
 * Model of the department picker fragment ({@code fragments/department-picker}).
 *
 * @param options            one option per department the signed-in person may choose
 * @param allPersonsSelected whether "all persons" is chosen instead of departments
 * @param allPersonsCount    number of persons shown when "all persons" is chosen
 */
public record DepartmentPickerDto(List<Option> options, boolean allPersonsSelected, int allPersonsCount) {

    public record Option(String name, int activeMemberCount, boolean selected) {
    }

    public List<String> selectedNames() {
        return options.stream()
            .filter(Option::selected)
            .map(Option::name)
            .toList();
    }
}
