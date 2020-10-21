package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class LocalDatePropertyEditorTest {

    private LocalDatePropertyEditor editor;

    @BeforeEach
    void setUp() {
        editor = new LocalDatePropertyEditor();
    }

    @Test
    void ensureCorrectFormattingWithDefaultPattern() {

        editor.setValue(LocalDate.of(2015, 12, 21));

        assertThat(editor.getAsText()).isEqualTo("21.12.2015");
    }

    @Test
    void ensureEmptyTextForNullDate() {

        editor.setValue(null);

        assertThat(editor.getAsText()).isEmpty();
    }

    @Test
    void ensureCorrectParsing() {

        editor.setAsText("13.12.2016");

        assertThat(editor.getValue()).isEqualTo(LocalDate.of(2016, 12, 13));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void ensureNullDateForEmptyText(String givenDateTextValue) {

        editor.setAsText(givenDateTextValue);

        assertThat(editor.getValue()).isNull();
    }

    @Test
    void ensureSettingTextRepresentingInvalidDateThrows() {
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText("foo"));
    }
}
