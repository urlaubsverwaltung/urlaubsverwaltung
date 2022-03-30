package org.synyx.urlaubsverwaltung.person.basedata;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonBasedataServiceImplTest {

    private PersonBasedataServiceImpl sut;

    @Mock
    private PersonBasedataRepository personBasedataRepository;

    @BeforeEach
    void setUp() {
        sut = new PersonBasedataServiceImpl(personBasedataRepository);
    }

    @Test
    void getBasedataByPersonId() {

        final PersonBasedataEntity personBasedataEntity = new PersonBasedataEntity();
        personBasedataEntity.setPersonId(1);
        personBasedataEntity.setPersonnelNumber("1337");
        personBasedataEntity.setAdditionalInformation("Some additional Information");

        when(personBasedataRepository.findById(1)).thenReturn(Optional.of(personBasedataEntity));

        final Optional<PersonBasedata> basedata = sut.getBasedataByPersonId(1);
        assertThat(basedata).hasValueSatisfying(personBasedata -> {
            assertThat(personBasedata.getPersonId()).isEqualTo(1);
            assertThat(personBasedata.getPersonnelNumber()).isEqualTo("1337");
            assertThat(personBasedata.getAdditionalInformation()).isEqualTo("Some additional Information");
        });
    }

    @Test
    void update() {

        final PersonBasedata personBasedata = new PersonBasedata(1, "1337", "Some additional Information");
        sut.update(personBasedata);

        final ArgumentCaptor<PersonBasedataEntity> captor = ArgumentCaptor.forClass(PersonBasedataEntity.class);
        verify(personBasedataRepository).save(captor.capture());
        final PersonBasedataEntity personBasedataEntity = captor.getValue();
        assertThat(personBasedataEntity.getPersonId()).isEqualTo(1);
        assertThat(personBasedataEntity.getPersonnelNumber()).isEqualTo("1337");
        assertThat(personBasedataEntity.getAdditionalInformation()).isEqualTo("Some additional Information");
    }
}
