package org.synyx.urlaubsverwaltung.web.thymeleaf;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.synyx.urlaubsverwaltung.web.AssetManifestService;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Optional;

class AssetAttributeTagProcessor extends AbstractAttributeTagProcessor {

    private static final int PRECEDENCE = 10000;

    private final String attributeName;
    private final AssetManifestService assetManifestService;

    AssetAttributeTagProcessor(String dialectPrefix, String attributeName, AssetManifestService assetManifestService) {
        super(TemplateMode.HTML, dialectPrefix, null, false, attributeName, true, PRECEDENCE, true);
        this.attributeName = attributeName;
        this.assetManifestService = assetManifestService;
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        final String contextPath = getCurrentHttpRequest().map(HttpServletRequest::getContextPath).orElse("");
        final String hashedAssetFilename = assetManifestService.getHashedAssetFilename(attributeValue, contextPath);
        structureHandler.setAttribute(this.attributeName, hashedAssetFilename);
    }

    private static Optional<HttpServletRequest> getCurrentHttpRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(ServletRequestAttributes.class::isInstance)
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest);
    }
}
