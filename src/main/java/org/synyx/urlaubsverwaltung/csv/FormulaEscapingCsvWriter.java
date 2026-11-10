package org.synyx.urlaubsverwaltung.csv;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Quotes every value, so that separators and line breaks stay in their cell, and keeps spreadsheet applications
 * from running a value as formula (CSV injection, see <a href="https://owasp.org/www-community/attacks/CSV_Injection">OWASP</a>),
 * e.g. a name like {@code =HYPERLINK(...)} coming from the identity provider.
 */
class FormulaEscapingCsvWriter extends CSVWriter {

    private static final Pattern FORMULA_START = Pattern.compile("^[=+\\-@\\t\\r]");
    // numbers formatted by DecimalFormat like -1,5 or -1.234,5 (or with non-breaking spaces as grouping separator)
    // start with a sign as well, but must stay numbers
    private static final Pattern NUMBER = Pattern.compile("[+-]?\\d[\\d.,\\u00a0\\u202f]*");

    FormulaEscapingCsvWriter(Writer writer, char separator) {
        super(writer, separator, DEFAULT_QUOTE_CHARACTER, DEFAULT_QUOTE_CHARACTER, DEFAULT_LINE_END);
    }

    @Override
    protected void writeNext(String[] nextLine, boolean applyQuotesToAll, Appendable appendable) throws IOException {
        final String[] escaped = nextLine == null ? null : Arrays.stream(nextLine).map(FormulaEscapingCsvWriter::escape).toArray(String[]::new);
        super.writeNext(escaped, true, appendable);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (FORMULA_START.matcher(value).find() && !NUMBER.matcher(value).matches()) {
            return "'" + value;
        }
        return value;
    }
}
