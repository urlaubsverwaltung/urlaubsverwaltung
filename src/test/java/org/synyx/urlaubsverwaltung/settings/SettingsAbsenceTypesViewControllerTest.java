package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdate;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.CYAN;

@ExtendWith(MockitoExtension.class)
class SettingsAbsenceTypesViewControllerTest {

    private SettingsAbsenceTypesViewController sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private SpecialLeaveSettingsService specialLeaveSettingsService;

    @BeforeEach
    void setUp() {
        sut = new SettingsAbsenceTypesViewController(settingsService, vacationTypeService, specialLeaveSettingsService);
    }

    @Test
    void ensureGetSettings() throws Exception {

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(
            new VacationType(1L, true, HOLIDAY, "message-key-1", true, true, CYAN, true)
        ));

        when(specialLeaveSettingsService.getSpecialLeaveSettings()).thenReturn(List.of(
            new SpecialLeaveSettingsItem(2L, true, "message-key-2", 3)
        ));

        when(settingsService.getSettings()).thenReturn(new Settings());

        final AbsenceTypeSettingsDto expectedAbsenceTypeSettingsDto = new AbsenceTypeSettingsDto();
        expectedAbsenceTypeSettingsDto.setItems(List.of(
            AbsenceTypeSettingsItemDto.builder()
                .setId(1L)
                .setActive(true)
                .setMessageKey("message-key-1")
                .setCategory(HOLIDAY)
                .setRequiresApprovalToApply(true)
                .setRequiresApprovalToCancel(true)
                .setColor(CYAN)
                .setVisibleToEveryone(true)
                .build()
        ));

        final SpecialLeaveSettingsItemDto item = new SpecialLeaveSettingsItemDto();
        item.setId(2L);
        item.setActive(true);
        item.setMessageKey("message-key-2");
        item.setDays(3);

        final SpecialLeaveSettingsDto expectedSpecialLeaveSettingsDto = new SpecialLeaveSettingsDto();
        expectedSpecialLeaveSettingsDto.setSpecialLeaveSettingsItems(List.of(item));

        perform(get("/web/settings/absence-types"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("settings", allOf(
                hasProperty("absenceTypeSettings", is(expectedAbsenceTypeSettingsDto)),
                hasProperty("specialLeaveSettings", is(expectedSpecialLeaveSettingsDto))
            )));
    }

    @Test
    void ensureSaveSettings() throws Exception {

        perform(post("/web/settings/absence-types")
            .param("id", "1337")
            .param("absenceTypeSettings.items[0].id", "1")
            .param("absenceTypeSettings.items[0].active", "true")
            .param("absenceTypeSettings.items[0].messageKey", "message-key-1")
            .param("absenceTypeSettings.items[0].category", "HOLIDAY")
            .param("absenceTypeSettings.items[0].requiresApprovalToApply", "true")
            .param("absenceTypeSettings.items[0].requiresApprovalToCancel", "true")
            .param("absenceTypeSettings.items[0].color", "CYAN")
            .param("absenceTypeSettings.items[0].visibleToEveryone", "true")
            .param("specialLeaveSettings.specialLeaveSettingsItems[0].id", "2")
            .param("specialLeaveSettings.specialLeaveSettingsItems[0].active", "true")
            .param("specialLeaveSettings.specialLeaveSettingsItems[0].messageKey", "message-key-2")
            .param("specialLeaveSettings.specialLeaveSettingsItems[0].days", "3")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/settings/absence-types"))
            .andExpect(flash().attribute("success", true));

        verify(vacationTypeService).updateVacationTypes(List.of(
            new VacationTypeUpdate(1L, true, true, true, CYAN, true)
        ));

        verify(specialLeaveSettingsService).saveAll(List.of(
            new SpecialLeaveSettingsItem(2L, true, "message-key-2", 3)
        ));

        verifyNoInteractions(settingsService);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
