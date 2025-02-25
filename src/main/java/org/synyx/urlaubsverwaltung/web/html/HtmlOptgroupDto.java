package org.synyx.urlaubsverwaltung.web.html;

import java.util.List;

public record HtmlOptgroupDto(
    String labelMessageKey,
    List<HtmlOptionDto> options
) {
}
