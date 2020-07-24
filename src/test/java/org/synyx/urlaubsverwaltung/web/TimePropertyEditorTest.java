package org.synyx.urlaubsverwaltung.web;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;

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

        Assert.assertNotNull("Should not be null", editor.getAsText());
        Assert.assertEquals("Wrong text representation", "11:23", editor.getAsText());
    }

    @Test
    void ensureEmptyTextForNullTime() {

        editor.setValue(null);

        Assert.assertEquals("Wrong text representation", "", editor.getAsText());
    }

    @Test
    void ensureCorrectParsing() {

        Time time = Time.valueOf("11:23:00");

        editor.setAsText("11:23");

        Assert.assertNotNull("Should not be null", editor.getValue());
        Assert.assertEquals("Wrong time", time, editor.getValue());
    }

    @Test
    void ensureNullTimeForEmptyText() {

        editor.setAsText("");

        Assert.assertNull("Should be null", editor.getValue());
    }

    @Test
    void ensureNullTimeForNullText() {

        editor.setAsText(null);

        Assert.assertNull("Should be null", editor.getValue());
    }

    @Test
    void ensureSettingTextRepresentingInvalidTimeThrows() {
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText("foo"));
    }
}
