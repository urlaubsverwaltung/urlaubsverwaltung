import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import inject from "@rollup/plugin-inject";
import dynamicImportVariables from "@rollup/plugin-dynamic-import-vars";
import styles from "rollup-plugin-styles";
import esbuild from "rollup-plugin-esbuild";
import assetsManifest from "./rollup-plugin-assets-manifest";
import clean from "./rollup-plugin-clean";
import glob from "fast-glob";

const NODE_ENV = process.env.NODE_ENV || "development";
const isProduction = NODE_ENV === "production";

const paths = {
  src: "src/main/javascript",
  dist: "target/classes/static/assets",
};

export default {
  input: {
    ...inputFiles(),
  },
  output: {
    dir: paths.dist,
    format: "es",
    sourcemap: true,
    entryFileNames: isProduction ? `[name].[hash].js` : `[name].js`,
    // custom assets like css files extracted by `rollup-plugin-styles`
    assetFileNames: "[name].[hash].[ext]",
  },
  moduleContext(id) {
    if (id.includes("@duetds")) {
      return "window";
    }
  },
  plugins: [
    clean({
      path: paths.dist,
    }),
    inject({
      $: "jquery",
      jQuery: "jquery",
    }),
    styles({
      extensions: [".css", ".less"],
      mode: "extract",
    }),
    // `@rollup/plugin-dynamic-import-vars` is required for duetds-datepicker (bundled with stencil and dynamic imports)
    dynamicImportVariables(),
    resolve({
      preferBuiltins: false,
      dedupe: ["jquery"],
    }),
    commonjs({
      // inject jquery results in a cjs import `require('juery')` in es modules.
      transformMixedEsModules: true,
      strictRequires: "debug",
    }),
    esbuild({
      sourceMap: true,
      minify: isProduction,
    }),
    assetsManifest({
      output: "src/main/webapp/WEB-INF/assets-manifest.json",
      publicPath: "/assets",
      key(key, entry) {
        if (entry.type === "asset" && key.startsWith("create-datepicker") && key.endsWith(".css")) {
          // datepicker css is bundled into the module which first imports it. (e.g. 'filter-modal')
          // rename it to datepicker.css since this asset handles datepicker styling.
          return "datepicker.css";
        }
      },
    }),
  ],
};

function inputFiles() {
  const files = glob.sync(`${paths.src}/bundles/*.js`);
  return Object.fromEntries(files.map((file) => [entryName(file), file]));
}

function entryName(file) {
  const filename = file.slice(Math.max(0, file.lastIndexOf("/") + 1));
  return filename.slice(0, Math.max(0, filename.lastIndexOf(".")));
}
