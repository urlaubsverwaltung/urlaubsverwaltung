package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.application.vacationtype.CustomVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeLabel;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdate;

import java.util.List;
import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OTHER;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.CYAN;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

@ExtendWith(MockitoExtension.class)
class SettingsAbsenceTypesViewControllerTest {

    private static final Locale LOCALE_DE_AT = Locale.forLanguageTag("de-AT");
    private static final Locale LOCALE_EL = Locale.forLanguageTag("el");

    private SettingsAbsenceTypesViewController sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private SpecialLeaveSettingsService specialLeaveSettingsService;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SettingsAbsenceTypesDtoValidator validator;

    @BeforeEach
    void setUp() {
        sut = new SettingsAbsenceTypesViewController(settingsService, vacationTypeService, specialLeaveSettingsService,
            messageSource, validator);
    }

    @Test
    void ensureGetSettings() throws Exception {

        final Locale locale = GERMAN;
        final MessageSource messageSource = messageSourceForVacationType("message-key-1", "label-1", locale);
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(
            ProvidedVacationType.builder(messageSource)
                .id(1L)
                .active(true)
                .category(HOLIDAY)
                .messageKey("message-key-1")
                .requiresApprovalToApply(true)
                .requiresApprovalToCancel(true)
                .color(CYAN)
                .visibleToEveryone(true)
                .build()
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
                .setLabel("label-1")
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

        perform(
            get("/web/settings/absence-types")
                .locale(locale)
        )
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
            .param("absenceTypeSettings.items[0].label", "label-1")
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
            new VacationTypeUpdate(1L, true, true, true, CYAN, true, null)
        ));

        verify(specialLeaveSettingsService).saveAll(List.of(
            new SpecialLeaveSettingsItem(2L, true, "message-key-2", 3)
        ));

        verifyNoInteractions(settingsService);
    }

    @Test
    void ensureAddAbsenceType() throws Exception {

        final AbsenceTypeSettingsItemDto existingAbsenceType = new AbsenceTypeSettingsItemDto();
        existingAbsenceType.setId(1L);
        existingAbsenceType.setActive(true);
        existingAbsenceType.setLabel("label-1");
        existingAbsenceType.setCategory(HOLIDAY);
        existingAbsenceType.setRequiresApprovalToApply(true);
        existingAbsenceType.setRequiresApprovalToCancel(true);
        existingAbsenceType.setVisibleToEveryone(true);
        existingAbsenceType.setColor(CYAN);

        final AbsenceTypeSettingsItemDto newAbsenceType = new AbsenceTypeSettingsItemDto();
        newAbsenceType.setActive(false);
        newAbsenceType.setLabel(null);
        newAbsenceType.setCategory(OTHER);
        newAbsenceType.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(GERMAN, ""),
            new AbsenceTypeSettingsItemLabelDto(LOCALE_DE_AT, ""),
            new AbsenceTypeSettingsItemLabelDto(ENGLISH, ""),
            new AbsenceTypeSettingsItemLabelDto(LOCALE_EL, "")
        ));
        newAbsenceType.setRequiresApprovalToApply(true);
        newAbsenceType.setRequiresApprovalToCancel(true);
        newAbsenceType.setVisibleToEveryone(false);
        newAbsenceType.setColor(YELLOW);

        final AbsenceTypeSettingsDto absenceTypeSettings = new AbsenceTypeSettingsDto();
        absenceTypeSettings.setItems(List.of(existingAbsenceType, newAbsenceType));

        final SpecialLeaveSettingsItemDto specialLeaveSettingsItem = new SpecialLeaveSettingsItemDto();
        specialLeaveSettingsItem.setId(2L);
        specialLeaveSettingsItem.setActive(true);
        specialLeaveSettingsItem.setMessageKey("message-key-2");
        specialLeaveSettingsItem.setDays(3);

        final SpecialLeaveSettingsDto specialLeaveSettings = new SpecialLeaveSettingsDto();
        specialLeaveSettings.setSpecialLeaveSettingsItems(List.of(specialLeaveSettingsItem));

        final SettingsAbsenceTypesDto expectedSettings = new SettingsAbsenceTypesDto();
        expectedSettings.setId(1337L);
        expectedSettings.setAbsenceTypeSettings(absenceTypeSettings);
        expectedSettings.setSpecialLeaveSettings(specialLeaveSettings);

        perform(
            post("/web/settings/absence-types")
                .param("add-absence-type", "")
                .param("id", "1337")
                .param("absenceTypeSettings.items[0].id", "1")
                .param("absenceTypeSettings.items[0].active", "true")
                .param("absenceTypeSettings.items[0].label", "label-1")
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
            .andExpect(status().isOk())
            .andExpect(model().attribute("settings", is(expectedSettings)))
            .andExpect(view().name("settings/absence-types/settings_absence_types"));

        verifyNoInteractions(vacationTypeService);
    }

    @Test
    void ensureAddAbsenceTypeWithTurboFrame() throws Exception {

        final AbsenceTypeSettingsItemDto newAbsenceType = new AbsenceTypeSettingsItemDto();
        newAbsenceType.setActive(false);
        newAbsenceType.setLabel(null);
        newAbsenceType.setCategory(OTHER);
        newAbsenceType.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(GERMAN, ""),
            new AbsenceTypeSettingsItemLabelDto(LOCALE_DE_AT, ""),
            new AbsenceTypeSettingsItemLabelDto(ENGLISH, ""),
            new AbsenceTypeSettingsItemLabelDto(LOCALE_EL, "")
        ));
        newAbsenceType.setRequiresApprovalToApply(true);
        newAbsenceType.setRequiresApprovalToCancel(true);
        newAbsenceType.setVisibleToEveryone(false);
        newAbsenceType.setColor(YELLOW);

        perform(
            post("/web/settings/absence-types")
                .param("add-absence-type", "")
                .header("Turbo-Frame", "frame-absence-type")
                .param("id", "1337")
                .param("absenceTypeSettings.items[0].id", "1")
                .param("absenceTypeSettings.items[0].active", "true")
                .param("absenceTypeSettings.items[0].label", "label-1")
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
            .andExpect(status().isOk())
            .andExpect(model().attribute("newAbsenceType", is(newAbsenceType)))
            .andExpect(model().attribute("newAbsenceTypeIndex", 1))
            .andExpect(model().attribute("frameNewAbsenceTypeRequested", true))
            .andExpect(view().name("settings/absence-types/absence-types::#frame-absence-type"));

        verifyNoInteractions(vacationTypeService);
    }

    @Test
    void ensureSaveNewAbsenceType() throws Exception {

        perform(
            post("/web/settings/absence-types")
                .param("id", "1337")
                .param("absenceTypeSettings.items[0].active", "true")
                .param("absenceTypeSettings.items[0].label", "label-1")
                .param("absenceTypeSettings.items[0].requiresApprovalToApply", "true")
                .param("absenceTypeSettings.items[0].requiresApprovalToCancel", "true")
                .param("absenceTypeSettings.items[0].color", "CYAN")
                .param("absenceTypeSettings.items[0].visibleToEveryone", "true")
                .param("absenceTypeSettings.items[0].labels[0].locale", "de")
                .param("absenceTypeSettings.items[0].labels[0].label", "label-deutsch")
                .param("absenceTypeSettings.items[0].labels[1].locale", "de_AT")
                .param("absenceTypeSettings.items[0].labels[1].label", "label-österreich")
                .param("specialLeaveSettings.specialLeaveSettingsItems[0].id", "2")
                .param("specialLeaveSettings.specialLeaveSettingsItems[0].active", "true")
                .param("specialLeaveSettings.specialLeaveSettingsItems[0].messageKey", "message-key-2")
                .param("specialLeaveSettings.specialLeaveSettingsItems[0].days", "3")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/settings/absence-types"))
            .andExpect(flash().attribute("success", true));

        verify(vacationTypeService).updateVacationTypes(List.of());

        verify(vacationTypeService).createVacationTypes(List.of(
            CustomVacationType.builder(messageSource)
                .active(true)
                .category(VacationCategory.OTHER)
                .requiresApprovalToApply(true)
                .requiresApprovalToCancel(true)
                .color(CYAN)
                .visibleToEveryone(true)
                .labels(List.of(
                    new VacationTypeLabel(GERMAN, "label-deutsch"),
                    new VacationTypeLabel(Locale.forLanguageTag("de_AT"), "label-österreich")
                ))
                .build()
        ));
    }

    private MessageSource messageSourceForVacationType(String messageKey, String label, Locale locale) {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(messageKey, new Object[]{}, locale)).thenReturn(label);
        return messageSource;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
