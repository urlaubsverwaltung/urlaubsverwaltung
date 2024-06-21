package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PreloadAssetProviderTest {

    private PreloadAssetProvider sut;

    @Mock
    private AssetManifestService assetManifestService;

    @BeforeEach
    void setUp() {
        sut = new PreloadAssetProvider(assetManifestService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"forward:", "redirect:"})
    @NullSource
    void doNotAssets(String viewName) {

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewName);

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModelMap().get("assets")).isNull();
    }

    @Test
    void doNotAssetsIfModelAndViewIsNull() {
        sut.postHandle(null, null, null, null);
        verifyNoInteractions(assetManifestService);
    }
}
