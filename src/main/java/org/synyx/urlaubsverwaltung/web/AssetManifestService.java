package org.synyx.urlaubsverwaltung.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Service
public class AssetManifestService {

    private static final String ASSETS_MANIFEST_FILE = "WEB-INF/assets-manifest.json";

    private final ResourceLoader resourceLoader;

    public AssetManifestService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Map<String, Asset> getAssetMap() {
        return getAssets(getManifestFile());
    }

    public String getHashedAssetFilename(String assetNameWithoutHash) {
        return getAsset(assetNameWithoutHash).getUrl();
    }

    public List<String> getAssetDependencies(String assetNameWithoutHash) {
        return getAsset(assetNameWithoutHash).getDependencies();
    }

    private Asset getAsset(String assetNameWithoutHash) {
        final Map<String, Asset> assets = getAssets(getManifestFile());
        if (assets.containsKey(assetNameWithoutHash)) {
            return assets.get(assetNameWithoutHash);
        }

        throw new IllegalStateException(format("could not resolve given asset name=%s", assetNameWithoutHash));
    }

    private Map<String, Asset> getAssets(InputStream manifest) {

        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(manifest, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("could not parse manifest json file");
        }
    }

    private InputStream getManifestFile() {
        try {
            return resourceLoader.getResource(ASSETS_MANIFEST_FILE).getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + ASSETS_MANIFEST_FILE);
        }
    }
}
