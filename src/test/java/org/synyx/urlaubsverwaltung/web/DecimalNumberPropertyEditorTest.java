package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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


    @ParameterizedTest
    @CsvSource({"de, '6,75'", "de, 6.75", "en, '6,75'", "en, 6.75"})
    void ensureNumberParsingWorksWithCommaAndDotSeparatedNumber(Locale locale, String text) {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(locale);

        propertyEditor.setAsText(text);
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


    @ParameterizedTest
    @ValueSource(strings = {"1e10000", "1e-10000", "1E+10000"})
    void ensureSettingTextWithAHugeExponentThrows(String text) {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);
        assertThatIllegalArgumentException().isThrownBy(() -> propertyEditor.setAsText(text));
    }

    @Test
    void ensureSettingTextWithTooManyDigitsThrows() {

        final String text = "9".repeat(101);

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);
        assertThatIllegalArgumentException().isThrownBy(() -> propertyEditor.setAsText(text));
    }

    @Test
    void ensureSettingTextWithASmallExponentWorks() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);

        propertyEditor.setAsText("1e2");
        assertThat(propertyEditor.getValue()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void ensureSettingTextNotRepresentingANumberThrows() {

        DecimalNumberPropertyEditor propertyEditor = new DecimalNumberPropertyEditor(Locale.ENGLISH);
        assertThatIllegalArgumentException().isThrownBy(() -> propertyEditor.setAsText("foo"));
    }
}
