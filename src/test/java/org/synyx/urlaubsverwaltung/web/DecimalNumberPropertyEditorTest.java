package org.synyx.urlaubsverwaltung.web;

import junit.framework.Assert;

import org.junit.Test;

import java.math.BigDecimal;

import java.util.Locale;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DecimalNumberPropertyEditorTest {

    @Test
    public void ensureCorrectNumberFormattingForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setValue(BigDecimal.valueOf(6.001));
        Assert.assertEquals("Wrong text representation", "6", propertyEditor.getAsText());

        propertyEditor.setValue(BigDecimal.valueOf(6.2342));
        Assert.assertEquals("Wrong text representation", "6,2", propertyEditor.getAsText());
    }


    @Test
    public void ensureCorrectNumberFormattingForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setValue(BigDecimal.valueOf(6.001));
        Assert.assertEquals("Wrong text representation", "6", propertyEditor.getAsText());

        propertyEditor.setValue(BigDecimal.valueOf(6.2342));
        Assert.assertEquals("Wrong text representation", "6.2", propertyEditor.getAsText());
    }


    @Test
    public void ensureCorrectNumberParsingForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setAsText("6");
        Assert.assertEquals("Wrong number", BigDecimal.valueOf(6.0), propertyEditor.getValue());

        propertyEditor.setAsText("6,5");
        Assert.assertEquals("Wrong number", BigDecimal.valueOf(6.5), propertyEditor.getValue());

        propertyEditor.setAsText("6,4223");
        Assert.assertEquals("Wrong number", BigDecimal.valueOf(6.4), propertyEditor.getValue());
    }


    @Test
    public void ensureCorrectNumberParsingForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("6");
        Assert.assertEquals("Wrong number", BigDecimal.valueOf(6.0), propertyEditor.getValue());

        propertyEditor.setAsText("6.5");
        Assert.assertEquals("Wrong number", BigDecimal.valueOf(6.5), propertyEditor.getValue());

        propertyEditor.setAsText("6.4223");
        Assert.assertEquals("Wrong number", BigDecimal.valueOf(6.4), propertyEditor.getValue());
    }


    @Test
    public void ensureEmptyTextForNullNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setValue(null);
        Assert.assertEquals("Wrong text representation", "", propertyEditor.getAsText());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureFormattingThrowsIfProvidingAnInvalidNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setValue("foo");
        propertyEditor.getAsText();
    }


    @Test
    public void ensureSettingEmptyTextResultsInNullNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("");
        Assert.assertNull("Should be null", propertyEditor.getValue());
    }


    @Test
    public void ensureSettingNullTextResultsInNullNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText(null);
        Assert.assertNull("Should be null", propertyEditor.getValue());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureSettingTextNotRepresentingANumberThrows() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("foo");
    }
}
