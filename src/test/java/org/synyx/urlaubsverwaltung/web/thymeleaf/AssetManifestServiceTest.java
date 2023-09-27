package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.synyx.urlaubsverwaltung.web.AssetManifestService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssetManifestServiceTest {

    @Test
    void getHashedAssetFilename() {

        final ResourceLoader resourceLoader = mock(ResourceLoader.class);
        final ClassPathResource manifest = new ClassPathResource("asset-manifest-service-manifest-file.json");
        when(resourceLoader.getResource("classpath:assets-manifest.json")).thenReturn(manifest);

        final AssetManifestService sut = new AssetManifestService(resourceLoader);

        String jsAsset = sut.getHashedAssetFilename("file-one.js", "");
        assertThat(jsAsset).isEqualTo("/public-path/file-one.contenthash.min.js");

        String cssAsset = sut.getHashedAssetFilename("file-one.css", "");
        assertThat(cssAsset).isEqualTo("/public-path/file-one.contenthash.css");
    }

    @Test
    void getHashedAssetFilenameThrowsWhenGivenNameDoesNotExistInManifestFile() {

        final ResourceLoader resourceLoader = mock(ResourceLoader.class);
        final ClassPathResource manifest = new ClassPathResource("asset-manifest-service-manifest-file-empty.json");
        when(resourceLoader.getResource("classpath:assets-manifest.json")).thenReturn(manifest);

        final AssetManifestService sut = new AssetManifestService(resourceLoader);

        assertThatIllegalStateException()
            .isThrownBy(() -> sut.getHashedAssetFilename("non-existent-filename", ""))
            .withMessage("could not resolve given asset name=non-existent-filename");
    }

    @Test
    void getHashedAssetFilenameThrowsWhenManifestFileCannotBeParsed() {

        final ResourceLoader resourceLoader = mock(ResourceLoader.class);
        final ClassPathResource manifest = new ClassPathResource("asset-manifest-service-manifest-file-invalid.json");

        when(resourceLoader.getResource("classpath:assets-manifest.json")).thenReturn(manifest);

        final AssetManifestService sut = new AssetManifestService(resourceLoader);

        assertThatIllegalStateException()
            .isThrownBy(() -> sut.getHashedAssetFilename("filename", ""))
            .withMessage("could not parse manifest json file");
    }

    @Test
    void getHashedAssetFilenameThrowsWhenManifestFileDoesNotExist() throws Exception {

        final Resource missingManifest = mock(Resource.class);
        when(missingManifest.getInputStream()).thenThrow(IOException.class);

        final ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResource(anyString())).thenReturn(missingManifest);

        final AssetManifestService sut = new AssetManifestService(resourceLoader);

        assertThatIllegalStateException()
            .isThrownBy(() -> sut.getHashedAssetFilename("filename", ""))
            .withMessage("could not read classpath:assets-manifest.json. please ensure 'npm run build' has been executed.");
    }
}
