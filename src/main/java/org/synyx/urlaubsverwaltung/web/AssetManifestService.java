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
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.hasText;

@Service
public class AssetManifestService {

    private static final String ASSETS_MANIFEST_FILE = "classpath:assets-manifest.json";

    private final ResourceLoader resourceLoader;

    public AssetManifestService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getHashedAssetFilename(String assetNameWithoutHash, String contextPath) {
        return getAsset(assetNameWithoutHash, contextPath).getUrl();
    }

    public Map<String, Asset> getAssets(String contextPath) {
        return readAssetManifest()
            .entrySet()
            .stream()
            .collect(toMap(Map.Entry::getKey, entry -> withContext(entry.getValue(), contextPath)));
    }

    private Asset getAsset(String assetNameWithoutHash, String contextPath) {

        final Map<String, Asset> assets = readAssetManifest();

        if (assets.containsKey(assetNameWithoutHash)) {
            return withContext(assets.get(assetNameWithoutHash), contextPath);
        }

        throw new IllegalStateException(format("could not resolve given asset name=%s", assetNameWithoutHash));
    }

    private Asset withContext(Asset asset, String contextPath) {
        final String assetUrl = withContext(asset.getUrl(), contextPath);
        final List<String> dependencies = asset.getDependencies().stream().map(url -> withContext(url, contextPath)).toList();
        return new Asset(assetUrl, dependencies);
    }

    private static String withContext(String url, String contextPath) {
        if (hasText(contextPath) && !"/".equals(contextPath)) {
            return contextPath + url;
        }
        return url;
    }

    private Map<String, Asset> readAssetManifest() {
        final InputStream manifest = getManifestFile();
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(manifest, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("could not parse manifest json file");
        }
    }

    private InputStream getManifestFile() {
        try {
            return resourceLoader.getResource(ASSETS_MANIFEST_FILE).getInputStream();
        } catch (IOException e) {
            final String message = String.format("could not read %s. please ensure 'npm run build' has been executed.", ASSETS_MANIFEST_FILE);
            throw new IllegalStateException(message);
        }
    }
}
