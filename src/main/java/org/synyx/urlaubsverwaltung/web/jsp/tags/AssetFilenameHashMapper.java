package org.synyx.urlaubsverwaltung.web.jsp.tags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class AssetFilenameHashMapper {

    private static final String ASSETS_MANIFEST_FILE = "WEB-INF/assets-manifest.json";

    private final ResourceLoader resourceLoader;

    AssetFilenameHashMapper(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    String getHashedAssetFilename(String assetNameWithoutHash) {

        File manifest = getManifestFile();
        HashMap<String, String> assets = getAssets(manifest);

        if (!assets.containsKey(assetNameWithoutHash)) {
            throw new IllegalStateException(String.format("could not resolve given asset name=%s", assetNameWithoutHash));
        }

        return assets.get(assetNameWithoutHash);

    }

    private HashMap<String, String> getAssets(File manifest) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(manifest, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("could not read manifest json file");
        }
    }

    private File getManifestFile() {
        try {
            return resourceLoader.getResource(ASSETS_MANIFEST_FILE).getFile();
        } catch (IOException e) {
            throw new IllegalStateException(String.format("could not read %s file", ASSETS_MANIFEST_FILE));
        }
    }
}
