package org.synyx.urlaubsverwaltung.web;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableList;

@Component
class PreloadAssetProvider implements HandlerInterceptor {

    private final AssetManifestService assetManifestService;

    PreloadAssetProvider(AssetManifestService assetManifestService) {
        this.assetManifestService = assetManifestService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && shouldAddAssets(modelAndView)) {
            final Map<String, List<PreloadAsset>> assets = assetManifestService.getAssetMap()
                .entrySet()
                .stream()
                .collect(
                    toMap(
                        Map.Entry::getKey,
                        o -> toPreloadAsset(o.getValue())
                    )
                );

            modelAndView.getModelMap().addAttribute("assets", assets);
        }
    }

    private static List<PreloadAsset> toPreloadAsset(Asset asset) {
        return asset.getDependencies().stream().map(d -> new PreloadAsset("script", d)).collect(toUnmodifiableList());
    }

    private boolean shouldAddAssets(ModelAndView modelAndView) {

        final String viewName = modelAndView.getViewName();
        if (viewName == null) {
            return false;
        }

        return !viewName.startsWith("redirect:");
    }
}
