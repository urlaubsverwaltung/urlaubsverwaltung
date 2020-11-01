package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class TimePropertyEditorTest {

    private TimePropertyEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TimePropertyEditor();
    }

    @Test
    void ensureCorrectFormatting() {

        editor.setValue(Time.valueOf("11:23:30"));

        assertThat(editor.getAsText()).isEqualTo("11:23");
    }

    @Test
    void ensureEmptyTextForNullTime() {

        editor.setValue(null);

        assertThat(editor.getAsText()).isEmpty();
    }

    @Test
    void ensureCorrectParsing() {

        Time time = Time.valueOf("11:23:00");

        editor.setAsText("11:23");

        assertThat(editor.getValue()).isEqualTo(time);
    }

    @Test
    void ensureNullTimeForEmptyText() {

        editor.setAsText("");

        assertThat(editor.getValue()).isNull();
    }

    @Test
    void ensureNullTimeForNullText() {

        editor.setAsText(null);

        assertThat(editor.getValue()).isNull();
    }

    @Test
    void ensureSettingTextRepresentingInvalidTimeThrows() {
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText("foo"));
    }
}
