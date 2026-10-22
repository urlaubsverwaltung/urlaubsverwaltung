package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class VacationTypeViewModelService {

    private final VacationTypeService vacationTypeService;

    VacationTypeViewModelService(VacationTypeService vacationTypeService) {
        this.vacationTypeService = vacationTypeService;
    }

    public List<VacationTypeDto> getVacationTypeColors() {
        return vacationTypeService.getAllVacationTypes().stream()
            .map(VacationTypeViewModelService::toVacationTypeDto)
            .toList();
    }

    /**
     * @return the labels of all vacation types by their id
     */
    public Map<Long, String> getVacationTypeLabels(Locale locale) {
        final Map<Long, String> labels = new LinkedHashMap<>();
        vacationTypeService.getAllVacationTypes().forEach(vacationType -> labels.put(vacationType.getId(), vacationType.getLabel(locale)));
        return labels;
    }

    private static VacationTypeDto toVacationTypeDto(VacationType<?> vacationType) {
        return new VacationTypeDto(vacationType.getId(), vacationType.getColor());
    }
}
