package org.synyx.urlaubsverwaltung.web.html;

public class HtmlOptionDto {

    private final String textMessageKey;
    private final String value;
    private final boolean selected;

    public HtmlOptionDto(String textMessageKey, String value, boolean selected) {
        this.textMessageKey = textMessageKey;
        this.value = value;
        this.selected = selected;
    }

    public String getTextMessageKey() {
        return textMessageKey;
    }

    public String getValue() {
        return value;
    }

    public boolean isSelected() {
        return selected;
    }
}
