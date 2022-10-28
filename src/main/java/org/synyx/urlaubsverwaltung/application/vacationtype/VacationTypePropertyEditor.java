package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;


/**
 * Convert {@link VacationType}'s id to {@link VacationType} object.
 */
public class VacationTypePropertyEditor extends PropertyEditorSupport {

    private final VacationTypeService vacationTypeService;

    public VacationTypePropertyEditor(VacationTypeService vacationTypeService) {
        this.vacationTypeService = vacationTypeService;
    }

    @Override
    public String getAsText() {

        if (this.getValue() == null) {
            return "";
        }

        return ((VacationType) this.getValue()).getId().toString();
    }


    @Override
    public void setAsText(String text) {

        if (!StringUtils.hasText(text)) {
            return;
        }

        vacationTypeService.getById(Integer.valueOf(text))
            .ifPresentOrElse(this::setValue, () -> setValue(null));
    }
}
