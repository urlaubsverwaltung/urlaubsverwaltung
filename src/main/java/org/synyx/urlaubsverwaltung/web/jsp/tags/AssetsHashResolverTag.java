package org.synyx.urlaubsverwaltung.web.jsp.tags;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

public class AssetsHashResolverTag extends RequestContextAwareTag {

    private String var;
    private String asset;

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    protected int doStartTagInternal() {

        AssetFilenameHashMapper assetFilenameHashMapper = getAssetManifestBean();
        String mappedAssetName = assetFilenameHashMapper.getHashedAssetFilename(this.asset);

        pageContext.setAttribute(this.var, mappedAssetName);

        return EVAL_PAGE;
    }

    private AssetFilenameHashMapper getAssetManifestBean() {

        WebApplicationContext webApplicationContext = getRequestContext().getWebApplicationContext();
        return new AssetFilenameHashMapper(webApplicationContext);
    }
}
