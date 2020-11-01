package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor}.
 */
class DecimalNumberPropertyEditorTest {

    @Test
    void ensureCorrectNumberFormattingForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setValue(new BigDecimal("3"));
        assertThat(propertyEditor.getAsText()).isEqualTo("3");

        propertyEditor.setValue(new BigDecimal("3.5"));
        assertThat(propertyEditor.getAsText()).isEqualTo("3,5");

        propertyEditor.setValue(new BigDecimal("3.50"));
        assertThat(propertyEditor.getAsText()).isEqualTo("3,5");

        propertyEditor.setValue(new BigDecimal("3.75"));
        assertThat(propertyEditor.getAsText()).isEqualTo("3,75");

        propertyEditor.setValue(BigDecimal.valueOf(3.001));
        assertThat(propertyEditor.getAsText()).isEqualTo("3");

        propertyEditor.setValue(BigDecimal.valueOf(3.2342));
        assertThat(propertyEditor.getAsText()).isEqualTo("3,23");
    }


    @Test
    void ensureCorrectNumberFormattingForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setValue(new BigDecimal("3"));
        assertThat(propertyEditor.getAsText()).isEqualTo("3");

        propertyEditor.setValue(new BigDecimal("3.5"));
        assertThat(propertyEditor.getAsText()).isEqualTo("3.5");

        propertyEditor.setValue(new BigDecimal("3.50"));
        assertThat(propertyEditor.getAsText()).isEqualTo("3.5");

        propertyEditor.setValue(new BigDecimal("3.75"));
        assertThat(propertyEditor.getAsText()).isEqualTo("3.75");

        propertyEditor.setValue(BigDecimal.valueOf(3.001));
        assertThat(propertyEditor.getAsText()).isEqualTo("3");

        propertyEditor.setValue(BigDecimal.valueOf(3.2342));
        assertThat(propertyEditor.getAsText()).isEqualTo("3.23");
    }


    @Test
    void ensureCorrectNumberParsingForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setAsText("6");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.00"));

        propertyEditor.setAsText("6,5");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.50"));

        propertyEditor.setAsText("6,75");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.75"));

        propertyEditor.setAsText("6,4223");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.42"));
    }


    @Test
    void ensureCorrectNumberParsingForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("6");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.00"));

        propertyEditor.setAsText("6.5");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.50"));

        propertyEditor.setAsText("6.75");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.75"));

        propertyEditor.setAsText("6.4223");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.42"));
    }


    @Test
    void ensureNumberParsingWorksWithCommaSeparatedNumberForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setAsText("6,75");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.75"));
    }


    @Test
    void ensureNumberParsingWorksWithDotSeparatedNumberForGermanLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setAsText("6.75");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.75"));
    }


    @Test
    void ensureNumberParsingWorksWithCommaSeparatedNumberForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("6,75");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.75"));
    }


    @Test
    void ensureNumberParsingWorksWithDotSeparatedNumberForEnglishLocale() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("6.75");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("6.75"));
    }


    @Test
    void ensureEmptyTextForNullNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.GERMAN);

        propertyEditor.setValue(null);
        assertThat(propertyEditor.getAsText()).isEmpty();
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
        assertThat(propertyEditor.getValue()).isNull();
    }


    @Test
    void ensureSettingNullTextResultsInNullNumber() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText(null);
        assertThat(propertyEditor.getValue()).isNull();
    }


    @Test
    void ensureSettingTextNotRepresentingANumberThrows() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);
        assertThatIllegalArgumentException().isThrownBy(() -> propertyEditor.setAsText("foo"));
    }
}
