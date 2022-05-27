package org.synyx.urlaubsverwaltung.application.vacationtype;

public class VacationTypeMapper {

    private VacationTypeMapper() {
        //
    }

    public static VacationTypeDto toVacationTypeDto(VacationType vacationType) {
        return new VacationTypeDto(vacationType.getId(), vacationType.getColor());
    }
}
