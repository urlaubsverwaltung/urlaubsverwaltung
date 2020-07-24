package org.synyx.urlaubsverwaltung.web;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor}.
 */
class DecimalNumberPropertyEditorTest {

    @Test
    void ensureCorrectNumberFormattingForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setValue(new BigDecimal("3"));
        Assert.assertEquals("Wrong text representation", "3", propertyEditor.getAsText());

        propertyEditor.setValue(new BigDecimal("3.5"));
        Assert.assertEquals("Wrong text representation", "3,5", propertyEditor.getAsText());

        propertyEditor.setValue(new BigDecimal("3.50"));
        Assert.assertEquals("Wrong text representation", "3,5", propertyEditor.getAsText());

        propertyEditor.setValue(new BigDecimal("3.75"));
        Assert.assertEquals("Wrong text representation", "3,75", propertyEditor.getAsText());

        propertyEditor.setValue(BigDecimal.valueOf(3.001));
        Assert.assertEquals("Wrong text representation", "3", propertyEditor.getAsText());

        propertyEditor.setValue(BigDecimal.valueOf(3.2342));
        Assert.assertEquals("Wrong text representation", "3,23", propertyEditor.getAsText());
    }


    @Test
    void ensureCorrectNumberFormattingForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setValue(new BigDecimal("3"));
        Assert.assertEquals("Wrong text representation", "3", propertyEditor.getAsText());

        propertyEditor.setValue(new BigDecimal("3.5"));
        Assert.assertEquals("Wrong text representation", "3.5", propertyEditor.getAsText());

        propertyEditor.setValue(new BigDecimal("3.50"));
        Assert.assertEquals("Wrong text representation", "3.5", propertyEditor.getAsText());

        propertyEditor.setValue(new BigDecimal("3.75"));
        Assert.assertEquals("Wrong text representation", "3.75", propertyEditor.getAsText());

        propertyEditor.setValue(BigDecimal.valueOf(3.001));
        Assert.assertEquals("Wrong text representation", "3", propertyEditor.getAsText());

        propertyEditor.setValue(BigDecimal.valueOf(3.2342));
        Assert.assertEquals("Wrong text representation", "3.23", propertyEditor.getAsText());
    }


    @Test
    void ensureCorrectNumberParsingForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setAsText("6");
        Assert.assertEquals("Wrong number", new BigDecimal("6.00"), propertyEditor.getValue());

        propertyEditor.setAsText("6,5");
        Assert.assertEquals("Wrong number", new BigDecimal("6.50"), propertyEditor.getValue());

        propertyEditor.setAsText("6,75");
        Assert.assertEquals("Wrong number", new BigDecimal("6.75"), propertyEditor.getValue());

        propertyEditor.setAsText("6,4223");
        Assert.assertEquals("Wrong number", new BigDecimal("6.42"), propertyEditor.getValue());
    }


    @Test
    void ensureCorrectNumberParsingForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("6");
        Assert.assertEquals("Wrong number", new BigDecimal("6.00"), propertyEditor.getValue());

        propertyEditor.setAsText("6.5");
        Assert.assertEquals("Wrong number", new BigDecimal("6.50"), propertyEditor.getValue());

        propertyEditor.setAsText("6.75");
        Assert.assertEquals("Wrong number", new BigDecimal("6.75"), propertyEditor.getValue());

        propertyEditor.setAsText("6.4223");
        Assert.assertEquals("Wrong number", new BigDecimal("6.42"), propertyEditor.getValue());
    }


    @Test
    void ensureNumberParsingWorksWithCommaSeparatedNumberForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setAsText("6,75");
        Assert.assertEquals("Wrong number", new BigDecimal("6.75"), propertyEditor.getValue());
    }


    @Test
    void ensureNumberParsingWorksWithDotSeparatedNumberForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setAsText("6.75");
        Assert.assertEquals("Wrong number", new BigDecimal("6.75"), propertyEditor.getValue());
    }


    @Test
    void ensureNumberParsingWorksWithCommaSeparatedNumberForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("6,75");
        Assert.assertEquals("Wrong number", new BigDecimal("6.75"), propertyEditor.getValue());
    }


    @Test
    void ensureNumberParsingWorksWithDotSeparatedNumberForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("6.75");
        Assert.assertEquals("Wrong number", new BigDecimal("6.75"), propertyEditor.getValue());
    }


    @Test
    void ensureEmptyTextForNullNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setValue(null);
        Assert.assertEquals("Wrong text representation", "", propertyEditor.getAsText());
    }


    @Test
    void ensureFormattingThrowsIfProvidingAnInvalidNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);
        propertyEditor.setValue("foo");

        assertThatIllegalArgumentException().isThrownBy(propertyEditor::getAsText);
    }


    @Test
    void ensureSettingEmptyTextResultsInNullNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("");
        Assert.assertNull("Should be null", propertyEditor.getValue());
    }


    @Test
    void ensureSettingNullTextResultsInNullNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText(null);
        Assert.assertNull("Should be null", propertyEditor.getValue());
    }


    @Test
    void ensureSettingTextNotRepresentingANumberThrows() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);
        assertThatIllegalArgumentException().isThrownBy(() -> propertyEditor.setAsText("foo"));
    }
}
