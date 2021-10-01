package org.synyx.urlaubsverwaltung.web.thymeleaf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static java.lang.String.format;

public class AssetFilenameHashMapper {

    private static final String ASSETS_MANIFEST_FILE = "WEB-INF/assets-manifest.json";

    private final ResourceLoader resourceLoader;

    public AssetFilenameHashMapper(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getHashedAssetFilename(String assetNameWithoutHash) {

        final HashMap<String, String> assets = getAssets(getManifestFile());
        if (!assets.containsKey(assetNameWithoutHash)) {
            throw new IllegalStateException(format("could not resolve given asset name=%s", assetNameWithoutHash));
        }

        return assets.get(assetNameWithoutHash);
    }

    private HashMap<String, String> getAssets(InputStream manifest) {

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
