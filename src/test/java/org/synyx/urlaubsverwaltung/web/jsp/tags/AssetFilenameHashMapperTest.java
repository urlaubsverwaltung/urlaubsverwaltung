package org.synyx.urlaubsverwaltung.web.jsp.tags;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssetFilenameHashMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getHashedAssetFilename() {

        final ResourceLoader resourceLoader = mock(ResourceLoader.class);
        final ClassPathResource manifest = new ClassPathResource("asset-filename-hash-mapper-manifest-file.json");
        when(resourceLoader.getResource("WEB-INF/assets-manifest.json")).thenReturn(manifest);

        final AssetFilenameHashMapper sut = new AssetFilenameHashMapper(resourceLoader);

        String jsAsset = sut.getHashedAssetFilename("file-one.js");
        assertThat(jsAsset).isEqualTo("/public-path/file-one.contenthash.min.js");

        String cssAsset = sut.getHashedAssetFilename("file-one.css");
        assertThat(cssAsset).isEqualTo("/public-path/file-one.contenthash.css");
    }

    @Test
    public void getHashedAssetFilenameThrowsWhenGivenNameDoesNotExistInManifestFile() {

        final ResourceLoader resourceLoader = mock(ResourceLoader.class);
        final ClassPathResource manifest = new ClassPathResource("asset-filename-hash-mapper-manifest-file-empty.json");
        when(resourceLoader.getResource("WEB-INF/assets-manifest.json")).thenReturn(manifest);

        final AssetFilenameHashMapper sut = new AssetFilenameHashMapper(resourceLoader);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("could not resolve given asset name=non-existent-filename");

        sut.getHashedAssetFilename("non-existent-filename");
    }

    @Test
    public void getHashedAssetFilenameThrowsWhenManifestFileCannotBeParsed() {

        final ResourceLoader resourceLoader = mock(ResourceLoader.class);
        final ClassPathResource manifest = new ClassPathResource("asset-filename-hash-mapper-manifest-file-invalid.json");

        when(resourceLoader.getResource("WEB-INF/assets-manifest.json")).thenReturn(manifest);

        final AssetFilenameHashMapper sut = new AssetFilenameHashMapper(resourceLoader);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("could not parse manifest json file");

        sut.getHashedAssetFilename("filename");
    }

    @Test
    public void getHashedAssetFilenameThrowsWhenManifestFileDoesNotExist() throws Exception {

        final Resource missingManifest = mock(Resource.class);
        when(missingManifest.getInputStream()).thenThrow(IOException.class);

        final ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResource(anyString())).thenReturn(missingManifest);

        final AssetFilenameHashMapper sut = new AssetFilenameHashMapper(resourceLoader);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("could not read WEB-INF/assets-manifest.json");

        sut.getHashedAssetFilename("filename");
    }
}
