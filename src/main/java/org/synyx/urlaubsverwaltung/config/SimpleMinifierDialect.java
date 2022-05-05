package org.synyx.urlaubsverwaltung.config;

import org.thymeleaf.dialect.IPostProcessorDialect;
import org.thymeleaf.engine.ITemplateHandler;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashSet;
import java.util.Set;

public class SimpleMinifierDialect implements IPostProcessorDialect {

    @Override
    public String getName() {
        return "simple-minifier";
    }

    @Override
    public int getDialectPostProcessorPrecedence() {
        return 1000;
    }

    @Override
    public Set<IPostProcessor> getPostProcessors() {
        final Set<IPostProcessor> set = new HashSet<>(1);
        set.add(new IPostProcessor() {
            @Override
            public TemplateMode getTemplateMode() {
                return TemplateMode.HTML;
            }

            @Override
            public int getPrecedence() {
                return 1000;
            }

            @Override
            public Class<? extends ITemplateHandler> getHandlerClass() {
                return SimpleMinifierHandler.class;
            }
        });
        return set;
    }
}
