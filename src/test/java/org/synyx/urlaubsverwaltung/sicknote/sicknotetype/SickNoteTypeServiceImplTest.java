package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

@ExtendWith(MockitoExtension.class)
class SickNoteTypeServiceImplTest {

    private SickNoteTypeServiceImpl sut;

    @Mock
    private SickNoteTypeRepository sickNoteTypeRepository;
    @Captor
    private ArgumentCaptor<List<SickNoteType>> sickNoteTypeArgumentCaptor;

    @BeforeEach
    void setUp() {
        sut = new SickNoteTypeServiceImpl(sickNoteTypeRepository);
    }

    @Test
    void ensureToReturnOrderedSickNoteTypes() {

        final SickNoteType sickNoteType = new SickNoteType();
        when(sickNoteTypeRepository.findAll(Sort.by("id"))).thenReturn(List.of(sickNoteType));
        final List<SickNoteType> sickNoteTypes = sut.getSickNoteTypes();

        assertThat(sickNoteTypes).containsExactly(sickNoteType);
    }

    @Test
    void ensureToCreateSickNoteTypesIfNotAlreadyCreated() {

        when(sickNoteTypeRepository.count()).thenReturn(0L);

        sut.insertDefaultSickNoteTypes();

        final SickNoteType sickNote = new SickNoteType();
        sickNote.setCategory(SICK_NOTE);
        sickNote.setMessageKey("application.data.sicknotetype.sicknote");

        final SickNoteType sickNoteChild = new SickNoteType();
        sickNoteChild.setCategory(SICK_NOTE_CHILD);
        sickNoteChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        verify(sickNoteTypeRepository).saveAll(sickNoteTypeArgumentCaptor.capture());
        final List<SickNoteType> sickNoteTypes = sickNoteTypeArgumentCaptor.getValue();
        assertThat(sickNoteTypes)
            .usingRecursiveFieldByFieldElementComparatorOnFields("category", "messageKey")
            .containsExactly(sickNote, sickNoteChild);
    }

    @Test
    void ensureToNotCreateSickNoteTypesIfAlreadyThere() {

        when(sickNoteTypeRepository.count()).thenReturn(2L);

        sut.insertDefaultSickNoteTypes();

        verify(sickNoteTypeRepository, Mockito.times(0)).saveAll(any());
    }
}
