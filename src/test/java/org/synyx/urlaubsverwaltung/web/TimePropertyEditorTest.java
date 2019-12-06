package org.synyx.urlaubsverwaltung.web;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Time;


public class TimePropertyEditorTest {

    private TimePropertyEditor editor;

    @Before
    public void setUp() {

        editor = new TimePropertyEditor();
    }


    @Test
    public void ensureCorrectFormatting() {

        editor.setValue(Time.valueOf("11:23:30"));

        Assert.assertNotNull("Should not be null", editor.getAsText());
        Assert.assertEquals("Wrong text representation", "11:23", editor.getAsText());
    }


    @Test
    public void ensureEmptyTextForNullTime() {

        editor.setValue(null);

        Assert.assertEquals("Wrong text representation", "", editor.getAsText());
    }


    @Test
    public void ensureCorrectParsing() {

        Time time = Time.valueOf("11:23:00");

        editor.setAsText("11:23");

        Assert.assertNotNull("Should not be null", editor.getValue());
        Assert.assertEquals("Wrong time", time, editor.getValue());
    }


    @Test
    public void ensureNullTimeForEmptyText() {

        editor.setAsText("");

        Assert.assertNull("Should be null", editor.getValue());
    }


    @Test
    public void ensureNullTimeForNullText() {

        editor.setAsText(null);

        Assert.assertNull("Should be null", editor.getValue());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureSettingTextRepresentingInvalidTimeThrows() {

        editor.setAsText("foo");
    }
}
