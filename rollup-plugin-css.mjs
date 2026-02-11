import postcss from "postcss";
import postcssLoadConfig from "postcss-load-config";

/**
 * Rollup plugin to process and extract CSS files using PostCSS.
 *
 * @param {Object} options - Plugin options
 * @param {string} options.extract - Output file path for extracted CSS (e.g., "css/common.css")
 * @returns {import('rollup').Plugin}
 */
export function css(options = {}) {
  const { extract } = options;

  if (!extract) {
    throw new Error("rollup-plugin-css: extract option is required");
  }

  let postcssConfig;
  const styles = new Map();

  return {
    name: "css",

    async buildStart() {
      // Load postcss.config.js once at build start
      try {
        postcssConfig = await postcssLoadConfig();
      } catch (error) {
        this.warn(`Failed to load PostCSS config: ${error.message}`);
        postcssConfig = { plugins: [] };
      }
    },

    async transform(code, id) {
      // Only process CSS files
      if (!id.endsWith(".css")) {
        return;
      }

      try {
        // Process CSS with PostCSS
        const result = await postcss(postcssConfig.plugins).process(code, {
          from: id,
          to: id,
          map: false,
        });

        // Store the processed CSS
        styles.set(id, result.css);

        // Handle warnings
        for (const warning of result.warnings()) {
          this.warn(warning.toString());
        }
      } catch (error) {
        this.error(`Failed to process CSS file ${id}: ${error.message}`);
      }

      // Return empty module - CSS is extracted, not included in JS bundle
      return {
        code: "export default {};",
        map: { mappings: "" },
      };
    },

    async generateBundle() {
      if (styles.size === 0) {
        return;
      }

      // Combine all processed CSS files
      const allCss = [...styles.values()].join("\n\n");

      // Emit the combined CSS as an asset
      this.emitFile({
        type: "asset",
        fileName: extract,
        source: allCss,
      });
    },
  };
}
