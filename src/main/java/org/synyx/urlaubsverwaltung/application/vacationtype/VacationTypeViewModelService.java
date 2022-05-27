package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class VacationTypeViewModelService {

    private final VacationTypeService vacationTypeService;

    VacationTypeViewModelService(VacationTypeService vacationTypeService) {
        this.vacationTypeService = vacationTypeService;
    }

    public List<VacationTypeDto> getVacationTypeColors() {
        return vacationTypeService.getAllVacationTypes().stream()
            .map(VacationTypeViewModelService::toVacationTypeDto)
            .collect(toList());
    }

    private static VacationTypeDto toVacationTypeDto(VacationType vacationType) {
        return new VacationTypeDto(vacationType.getId(), vacationType.getColor());
    }
}
