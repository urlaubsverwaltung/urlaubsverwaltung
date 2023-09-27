package org.synyx.urlaubsverwaltung.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.web.html.PreloadLink;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Component
class PreloadAssetProvider implements HandlerInterceptor {

    private final AssetManifestService assetManifestService;

    PreloadAssetProvider(AssetManifestService assetManifestService) {
        this.assetManifestService = assetManifestService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && shouldAddAssets(modelAndView)) {
            final Map<String, List<PreloadLink>> assets = assetManifestService.getAssets(request.getContextPath())
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, o -> toPreloadAsset(o.getValue())));

            modelAndView.getModelMap().addAttribute("assets", assets);
        }
    }

    private static List<PreloadLink> toPreloadAsset(Asset asset) {
        return asset.getDependencies().stream().map(d -> new PreloadLink("script", d)).toList();
    }

    private boolean shouldAddAssets(ModelAndView modelAndView) {

        final String viewName = modelAndView.getViewName();
        if (viewName == null) {
            return false;
        }

        return !viewName.startsWith("redirect:");
    }
}
