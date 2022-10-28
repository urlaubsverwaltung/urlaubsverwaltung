package org.synyx.urlaubsverwaltung.web.html;

import java.util.List;

public class HtmlSelectDto {

    private final List<HtmlOptgroupDto> optgroups;

    public HtmlSelectDto(List<HtmlOptgroupDto> optgroups) {
        this.optgroups = optgroups;
    }

    public List<HtmlOptgroupDto> getOptgroups() {
        return optgroups;
    }
}
