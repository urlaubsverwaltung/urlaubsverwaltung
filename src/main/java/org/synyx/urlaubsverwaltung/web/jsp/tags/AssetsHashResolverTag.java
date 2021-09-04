package org.synyx.urlaubsverwaltung.web.jsp.tags;

import org.springframework.web.context.WebApplicationContext;
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

        AssetFilenameHashMapper assetFilenameHashMapper = getAssetManifestBean();
        String mappedAssetName = assetFilenameHashMapper.getHashedAssetFilename(this.value);

        urlTag.setValue(mappedAssetName);

        return urlTag.doEndTag();
    }

    private AssetFilenameHashMapper getAssetManifestBean() {

        WebApplicationContext webApplicationContext = getRequestContext().getWebApplicationContext();
        return new AssetFilenameHashMapper(webApplicationContext);
    }
}
