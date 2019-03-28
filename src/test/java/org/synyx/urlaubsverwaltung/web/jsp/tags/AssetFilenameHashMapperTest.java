package org.synyx.urlaubsverwaltung.web.jsp.tags;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssetFilenameHashMapperTest {

    @Test
    public void getHashedAssetFilename() {

        ResourceLoader resourceLoader = mock(ResourceLoader.class);

        ClassPathResource manifest = new ClassPathResource("asset-filename-hash-mapper-manifest-file.json");
        when(resourceLoader.getResource("WEB-INF/assets-manifest.json")).thenReturn(manifest);

        AssetFilenameHashMapper sut = new AssetFilenameHashMapper(resourceLoader);
        String jsAsset = sut.getHashedAssetFilename("file-one.js");
        String cssAsset = sut.getHashedAssetFilename("file-one.css");

        assertThat(jsAsset).isEqualTo("/public-path/file-one.contenthash.min.js");
        assertThat(cssAsset).isEqualTo("/public-path/file-one.contenthash.css");
    }
}
