package org.synyx.urlaubsverwaltung.web.html;

import java.util.List;

public class HtmlOptgroupDto {

    private final String labelMessageKey;
    private final List<HtmlOptionDto> options;

    public HtmlOptgroupDto(String labelMessageKey, List<HtmlOptionDto> options) {
        this.labelMessageKey = labelMessageKey;
        this.options = options;
    }

    public String getLabelMessageKey() {
        return labelMessageKey;
    }

    public List<HtmlOptionDto> getOptions() {
        return options;
    }
}
