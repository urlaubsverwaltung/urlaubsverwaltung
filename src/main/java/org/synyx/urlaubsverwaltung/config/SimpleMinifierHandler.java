package org.synyx.urlaubsverwaltung.config;


import org.thymeleaf.engine.AbstractTemplateHandler;
import org.thymeleaf.model.IComment;
import org.thymeleaf.model.IText;
import org.thymeleaf.util.StringUtils;

import java.util.regex.Pattern;

public class SimpleMinifierHandler extends AbstractTemplateHandler {

    private static final Pattern TAB_OR_NEW_LINE = Pattern.compile("[\\t\\n]+\\s");

    @Override
    public void handleComment(IComment comment) {
        // do not print comments at all
    }

    @Override
    public void handleText(IText text) {
        // ignore white spaces, tabs and new lines
        if (!ignorable(text)) {
            super.handleText(text);
        }
    }

    private boolean ignorable(IText text) {
        return StringUtils.isEmptyOrWhitespace(text.getText()) || TAB_OR_NEW_LINE.matcher(text.getText()).matches();
    }
}
