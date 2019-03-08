package org.synyx.urlaubsverwaltung.web;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DateMidnightPropertyEditorTest {

    private DateMidnightPropertyEditor editor;

    @Before
    public void setUp() {

        editor = new DateMidnightPropertyEditor();
    }


    @Test
    public void ensureCorrectFormatting() {

        editor.setValue(new DateMidnight(2015, 12, 21));

        Assert.assertNotNull("Should not be null", editor.getAsText());
        Assert.assertEquals("Wrong text representation", "21.12.2015", editor.getAsText());
    }


    @Test
    public void ensureEmptyTextForNullDate() {

        editor.setValue(null);

        Assert.assertEquals("Wrong text representation", "", editor.getAsText());
    }


    @Test
    public void ensureCorrectParsing() {

        editor.setAsText("13.12.2016");

        Assert.assertNotNull("Should not be null", editor.getValue());
        Assert.assertEquals("Wrong date", new DateMidnight(2016, 12, 13), editor.getValue());
    }


    @Test
    public void ensureNullDateForEmptyText() {

        editor.setAsText("");

        Assert.assertNull("Should be null", editor.getValue());
    }


    @Test
    public void ensureNullDateForNullText() {

        editor.setAsText(null);

        Assert.assertNull("Should be null", editor.getValue());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureSettingTextRepresentingInvalidDateThrows() {

        editor.setAsText("foo");
    }
}
