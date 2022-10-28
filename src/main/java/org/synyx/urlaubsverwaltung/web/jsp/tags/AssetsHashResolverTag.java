package org.synyx.urlaubsverwaltung.web.jsp.tags;

import org.springframework.web.servlet.tags.RequestContextAwareTag;
import org.springframework.web.servlet.tags.UrlTag;
import org.synyx.urlaubsverwaltung.web.thymeleaf.AssetFilenameHashMapper;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

public class AssetsHashResolverTag extends RequestContextAwareTag {

    private String value;
    private final UrlTag urlTag;

    public AssetsHashResolverTag() {
        this.urlTag = new UrlTag();
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int doStartTagInternal() throws JspException {
        return this.urlTag.doStartTag();
    }

    @Override
    public void setPageContext(PageContext pageContext) {
        super.setPageContext(pageContext);
        this.urlTag.setPageContext(pageContext);
    }

    @Override
    public int doEndTag() throws JspException {
        urlTag.setValue(getAssetManifestBean().getHashedAssetFilename(this.value));
        return urlTag.doEndTag();
    }

    private AssetFilenameHashMapper getAssetManifestBean() {
        return new AssetFilenameHashMapper(getRequestContext().getWebApplicationContext());
    }
}
