package org.synyx.urlaubsverwaltung.web;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class LocalDatePropertyEditorTest {

    private LocalDatePropertyEditor editor;

    @BeforeEach
    void setUp() {
        editor = new LocalDatePropertyEditor();
    }

    @Test
    void ensureCorrectFormatting() {

        editor.setValue(LocalDate.of(2015, 12, 21));

        Assert.assertNotNull("Should not be null", editor.getAsText());
        Assert.assertEquals("Wrong text representation", "21.12.2015", editor.getAsText());
    }

    @Test
    void ensureEmptyTextForNullDate() {

        editor.setValue(null);

        Assert.assertEquals("Wrong text representation", "", editor.getAsText());
    }

    @Test
    void ensureCorrectParsing() {

        editor.setAsText("13.12.2016");

        Assert.assertNotNull("Should not be null", editor.getValue());
        Assert.assertEquals("Wrong date", LocalDate.of(2016, 12, 13), editor.getValue());
    }

    @Test
    void ensureNullDateForEmptyText() {

        editor.setAsText("");

        Assert.assertNull("Should be null", editor.getValue());
    }

    @Test
    void ensureNullDateForNullText() {

        editor.setAsText(null);

        Assert.assertNull("Should be null", editor.getValue());
    }

    @Test
    void ensureSettingTextRepresentingInvalidDateThrows() {
        assertThatIllegalArgumentException().isThrownBy(() -> editor.setAsText("foo"));
    }
}
