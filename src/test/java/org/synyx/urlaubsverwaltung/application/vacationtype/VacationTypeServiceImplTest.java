package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.Locale.JAPANESE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OTHER;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.BLUE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

@ExtendWith(MockitoExtension.class)
class VacationTypeServiceImplTest {

    public static final Locale LOCALE_DE_AT = Locale.forLanguageTag("de-AT");
    public static final Locale LOCALE_EL = Locale.forLanguageTag("el");
    private VacationTypeServiceImpl sut;

    @Mock
    private VacationTypeRepository vacationTypeRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        sut = new VacationTypeServiceImpl(vacationTypeRepository, applicationEventPublisher, messageSource);
    }

    @Test
    void getActiveVacationTypesFilteredBy() {

        final VacationTypeEntity holiday = new VacationTypeEntity();
        holiday.setId(1L);
        holiday.setCategory(HOLIDAY);
        holiday.setActive(true);

        final VacationTypeEntity overtime = new VacationTypeEntity();
        overtime.setId(2L);
        overtime.setCategory(OVERTIME);
        overtime.setActive(true);

        final VacationTypeEntity overtimeActive = new VacationTypeEntity();
        overtimeActive.setId(3L);
        overtimeActive.setCategory(OVERTIME);
        overtimeActive.setActive(true);

        when(vacationTypeRepository.findByActiveIsTrueOrderById()).thenReturn(List.of(holiday, overtimeActive, overtime));

        final List<VacationType<?>> typesWithoutCategory = sut.getActiveVacationTypesWithoutCategory(OVERTIME);
        assertThat(typesWithoutCategory).hasSize(1);
        assertThat(typesWithoutCategory.get(0).getId()).isEqualTo(1);
    }

    @Test
    void getActiveVacationTypes() {

        final VacationTypeEntity holiday = new VacationTypeEntity();
        holiday.setId(1L);
        holiday.setCategory(HOLIDAY);
        holiday.setActive(true);

        final VacationTypeEntity overtimeActive = new VacationTypeEntity();
        overtimeActive.setId(2L);
        overtimeActive.setCategory(OVERTIME);
        overtimeActive.setActive(true);

        when(vacationTypeRepository.findByActiveIsTrueOrderById()).thenReturn(List.of(holiday, overtimeActive));

        final List<VacationType<?>> activeVacationTypes = sut.getActiveVacationTypes();
        assertThat(activeVacationTypes).hasSize(2);
        assertThat(activeVacationTypes.get(0).getId()).isEqualTo(1);
        assertThat(activeVacationTypes.get(1).getId()).isEqualTo(2);
    }

    @Test
    void getAllVacationTypes() {

        final VacationTypeEntity holiday = new VacationTypeEntity();
        holiday.setId(1L);
        holiday.setCategory(HOLIDAY);
        holiday.setActive(true);

        final VacationTypeEntity overtime = new VacationTypeEntity();
        overtime.setId(2L);
        overtime.setCategory(OVERTIME);
        overtime.setActive(false);

        when(vacationTypeRepository.findAll(Sort.by("id"))).thenReturn(List.of(holiday, overtime));

        final List<VacationType<?>> allVacationTypes = sut.getAllVacationTypes();
        assertThat(allVacationTypes).hasSize(2);
        assertThat(allVacationTypes.get(0).getId()).isEqualTo(1);
        assertThat(allVacationTypes.get(1).getId()).isEqualTo(2);
    }

    @Test
    void getAllVacationTypesSortsLabelsDeterministically() {

        final VacationTypeEntity entity = new VacationTypeEntity();
        entity.setCustom(true);
        entity.setLabelByLocale(Map.of(
            // SupportedLanguage
            ENGLISH, "englisch",
            LOCALE_DE_AT, "österreichisch",
            GERMAN, "deutsch",
            LOCALE_EL, "griechisch",
            // not a SupportedLanguage
            JAPANESE, "japanisch"
        ));

        when(vacationTypeRepository.findAll(Sort.by("id"))).thenReturn(List.of(entity));

        final List<VacationType<?>> allVacationTypes = sut.getAllVacationTypes();
        assertThat(allVacationTypes.get(0)).satisfies(vacationType -> {
            assertThat(vacationType).isInstanceOf(CustomVacationType.class);

            final CustomVacationType customVacationType = (CustomVacationType) vacationType;
            assertThat(customVacationType.labels()).containsExactly(
                new VacationTypeLabel(GERMAN, "deutsch"),
                new VacationTypeLabel(LOCALE_DE_AT, "österreichisch"),
                new VacationTypeLabel(ENGLISH, "englisch"),
                new VacationTypeLabel(LOCALE_EL, "griechisch"),
                new VacationTypeLabel(JAPANESE, "japanisch")
            );
        });
    }

    @Test
    void ensureUpdateVacationTypesUpdatesTheGivenVacationTypes() {
        final VacationTypeEntity holidayEntity = new VacationTypeEntity();
        holidayEntity.setId(1L);
        holidayEntity.setCategory(HOLIDAY);
        holidayEntity.setMessageKey("holiday.message.key");
        holidayEntity.setActive(false);
        holidayEntity.setRequiresApprovalToApply(false);
        holidayEntity.setVisibleToEveryone(false);

        final VacationTypeEntity overtimeEntity = new VacationTypeEntity();
        overtimeEntity.setId(2L);
        overtimeEntity.setCategory(OVERTIME);
        overtimeEntity.setMessageKey("overtime.message.key");
        overtimeEntity.setActive(false);
        overtimeEntity.setRequiresApprovalToApply(false);
        overtimeEntity.setVisibleToEveryone(false);

        final VacationTypeEntity specialLeaveEntity = new VacationTypeEntity();
        specialLeaveEntity.setId(3L);
        specialLeaveEntity.setCategory(SPECIALLEAVE);
        specialLeaveEntity.setMessageKey("specialleave.message.key");
        specialLeaveEntity.setActive(true);
        specialLeaveEntity.setRequiresApprovalToApply(false);
        specialLeaveEntity.setVisibleToEveryone(true);

        when(vacationTypeRepository.findAllById(Set.of(1L, 2L, 3L))).thenReturn(List.of(holidayEntity, overtimeEntity, specialLeaveEntity));

        sut.updateVacationTypes(List.of(
            new VacationTypeUpdate(1L, true, false, false, YELLOW, false, List.of()),
            new VacationTypeUpdate(2L, false, true, true, YELLOW, false, List.of()),
            new VacationTypeUpdate(3L, true, true, true, YELLOW, true, List.of())
        ));

        @SuppressWarnings("unchecked") final ArgumentCaptor<List<VacationTypeEntity>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(vacationTypeRepository).saveAll(argumentCaptor.capture());

        final List<VacationTypeEntity> persistedList = argumentCaptor.getValue();
        assertThat(persistedList.get(0).getId()).isEqualTo(1);
        assertThat(persistedList.get(0).getCategory()).isEqualTo(HOLIDAY);
        assertThat(persistedList.get(0).getMessageKey()).isEqualTo("holiday.message.key");
        assertThat(persistedList.get(0).isActive()).isTrue();
        assertThat(persistedList.get(0).isRequiresApprovalToApply()).isFalse();
        assertThat(persistedList.get(0).isVisibleToEveryone()).isFalse();
        assertThat(persistedList.get(1).getId()).isEqualTo(2);
        assertThat(persistedList.get(1).getCategory()).isEqualTo(OVERTIME);
        assertThat(persistedList.get(1).getMessageKey()).isEqualTo("overtime.message.key");
        assertThat(persistedList.get(1).isActive()).isFalse();
        assertThat(persistedList.get(1).isRequiresApprovalToApply()).isTrue();
        assertThat(persistedList.get(1).isVisibleToEveryone()).isFalse();
        assertThat(persistedList.get(2).getId()).isEqualTo(3);
        assertThat(persistedList.get(2).getCategory()).isEqualTo(SPECIALLEAVE);
        assertThat(persistedList.get(2).getMessageKey()).isEqualTo("specialleave.message.key");
        assertThat(persistedList.get(2).isActive()).isTrue();
        assertThat(persistedList.get(2).isRequiresApprovalToApply()).isTrue();
        assertThat(persistedList.get(2).isVisibleToEveryone()).isTrue();

    }

    @Test
    void ensureUpdateVacationTypesForEmptyList() {
        when(vacationTypeRepository.findAllById(emptySet())).thenReturn(emptyList());

        sut.updateVacationTypes(List.of());

        verify(vacationTypeRepository).saveAll(emptyList());
    }

    @Test
    void ensureCreateVacationTypesIgnoresElementsWithIds() {
        sut.createVacationTypes(List.of(CustomVacationType.builder(messageSource).id(1L).labels(List.of()).build()));
        verify(vacationTypeRepository).saveAll(List.of());
    }

    @Test
    void ensureCreateVacationTypes() {

        sut.createVacationTypes(List.of(
            CustomVacationType.builder(messageSource)
                .active(true)
                .category(OTHER)
                .requiresApprovalToApply(true)
                .requiresApprovalToCancel(true)
                .color(YELLOW)
                .visibleToEveryone(true)
                .labels(List.of(
                    new VacationTypeLabel(GERMAN, "jokertag"),
                    new VacationTypeLabel(ENGLISH, "jokerday")
                ))
                .build(),
            CustomVacationType.builder(messageSource)
                .active(false)
                .category(HOLIDAY)
                .requiresApprovalToApply(false)
                .requiresApprovalToCancel(false)
                .color(BLUE)
                .visibleToEveryone(false)
                .labels(List.of(
                    new VacationTypeLabel(GERMAN, "familientag"),
                    new VacationTypeLabel(ENGLISH, "family day")
                ))
                .build()
        ));

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<VacationTypeEntity>> captor = ArgumentCaptor.forClass(List.class);

        verify(vacationTypeRepository).saveAll(captor.capture());

        final List<VacationTypeEntity> actual = captor.getValue();
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0)).satisfies(entity -> {
            assertThat(entity.isActive()).isTrue();
            assertThat(entity.getCategory()).isEqualTo(OTHER);
            assertThat(entity.isRequiresApprovalToApply()).isTrue();
            assertThat(entity.isRequiresApprovalToCancel()).isTrue();
            assertThat(entity.getColor()).isEqualTo(YELLOW);
            assertThat(entity.isVisibleToEveryone()).isTrue();
            assertThat(entity.getLabelByLocale()).containsExactlyInAnyOrderEntriesOf(Map.of(
                Locale.GERMAN, "jokertag",
                ENGLISH, "jokerday"
            ));
            assertThat(entity.getMessageKey()).isNull();
        });
        assertThat(actual.get(1)).satisfies(entity -> {
            assertThat(entity.isActive()).isFalse();
            assertThat(entity.getCategory()).isEqualTo(HOLIDAY);
            assertThat(entity.isRequiresApprovalToApply()).isFalse();
            assertThat(entity.isRequiresApprovalToCancel()).isFalse();
            assertThat(entity.getColor()).isEqualTo(BLUE);
            assertThat(entity.isVisibleToEveryone()).isFalse();
            assertThat(entity.getLabelByLocale()).containsExactlyInAnyOrderEntriesOf(Map.of(
                Locale.GERMAN, "familientag",
                ENGLISH, "family day"
            ));
            assertThat(entity.getMessageKey()).isNull();
        });
    }

    @Test
    void ensureCreatePublishesEvent() {

        final VacationTypeEntity firstEntity = new VacationTypeEntity();
        firstEntity.setId(1L);
        firstEntity.setActive(true);
        firstEntity.setCategory(OTHER);
        firstEntity.setRequiresApprovalToApply(true);
        firstEntity.setRequiresApprovalToCancel(true);
        firstEntity.setColor(YELLOW);
        firstEntity.setVisibleToEveryone(true);
        firstEntity.setLabelByLocale(Map.of(GERMAN, "jokertag", ENGLISH, "jokerday"));
        firstEntity.setCustom(true);

        final VacationTypeEntity secondEntity = new VacationTypeEntity();
        secondEntity.setId(2L);
        secondEntity.setActive(false);
        secondEntity.setCategory(HOLIDAY);
        secondEntity.setRequiresApprovalToApply(false);
        secondEntity.setRequiresApprovalToCancel(false);
        secondEntity.setColor(BLUE);
        secondEntity.setVisibleToEveryone(false);
        secondEntity.setLabelByLocale(Map.of(GERMAN, "familientag", ENGLISH, "family day"));
        secondEntity.setCustom(true);

        when(vacationTypeRepository.saveAll(anyList())).thenReturn(List.of(firstEntity, secondEntity));

        sut.createVacationTypes(List.of(
            CustomVacationType.builder(messageSource).labels(List.of()).build(),
            CustomVacationType.builder(messageSource).labels(List.of()).build()
        ));

        final ArgumentCaptor<VacationTypeCreatedEvent> captor = ArgumentCaptor.forClass(VacationTypeCreatedEvent.class);
        verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());

        final List<VacationTypeCreatedEvent> actualPublishedEvents = captor.getAllValues();
        assertThat(actualPublishedEvents).hasSize(2);
        assertThat(actualPublishedEvents.getFirst().vacationType()).satisfies(vacationType -> {
            assertThat(vacationType.getId()).isEqualTo(1L);
            assertThat(vacationType.active).isTrue();
            assertThat(vacationType.getCategory()).isEqualTo(OTHER);
            assertThat(vacationType.isRequiresApprovalToApply()).isTrue();
            assertThat(vacationType.isRequiresApprovalToCancel()).isTrue();
            assertThat(vacationType.getColor()).isEqualTo(YELLOW);
            assertThat(vacationType.isVisibleToEveryone()).isTrue();
            assertThat(vacationType.getLabel(GERMAN)).isEqualTo("jokertag");
            assertThat(vacationType.getLabel(ENGLISH)).isEqualTo("jokerday");
        });
        assertThat(actualPublishedEvents.get(1).vacationType()).satisfies(vacationType -> {
            assertThat(vacationType.getId()).isEqualTo(2L);
            assertThat(vacationType.active).isFalse();
            assertThat(vacationType.getCategory()).isEqualTo(HOLIDAY);
            assertThat(vacationType.isRequiresApprovalToApply()).isFalse();
            assertThat(vacationType.isRequiresApprovalToCancel()).isFalse();
            assertThat(vacationType.getColor()).isEqualTo(BLUE);
            assertThat(vacationType.isVisibleToEveryone()).isFalse();
            assertThat(vacationType.getLabel(GERMAN)).isEqualTo("familientag");
            assertThat(vacationType.getLabel(ENGLISH)).isEqualTo("family day");
        });
    }
}
