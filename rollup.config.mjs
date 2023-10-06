import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import replace from "@rollup/plugin-replace";
import inject from "@rollup/plugin-inject";
import dynamicImportVariables from "@rollup/plugin-dynamic-import-vars";
import postcss from "rollup-plugin-postcss";
import esbuild from "rollup-plugin-esbuild";
import { assetsManifest } from "./rollup-plugin-assets-manifest.mjs";
import { rimraf } from "rimraf";
import glob from "fast-glob";
import { dirname } from "node:path";
import { fileURLToPath } from "node:url";

const NODE_ENV = process.env.NODE_ENV;
const MODE = process.env.MODE || NODE_ENV || "development";
const isProduction = MODE === "production";

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const paths = {
  src: "src/main/javascript",
  dist: "target/classes/static/assets",
};

export default {
  input: {
    custom_elements_polyfill: `@ungap/custom-elements`,
    ...inputFiles(),
  },
  output: {
    dir: paths.dist,
    format: "es",
    sourcemap: true,
    entryFileNames: isProduction ? `[name].[hash].js` : `[name].js`,
    // custom assets like css files extracted by `rollup-plugin-styles`
    assetFileNames: "[name].[hash].[ext]",
    manualChunks(id) {
      if (id.includes("node_modules")) {
        const packageName = id.match(/[/\\]node_modules[/\\](.*?)([/\\]|$)/)[1];

        if (/node_modules\/jquery-ui\/ui\/i18n/.test(id)) {
          const locale = id.match(/datepicker-(\w\w)/)[1];
          // build separate bundles for jquery-ui-datepicker
          // which can be included on demand in the view templates
          // or used as dynamic import and handled by webpack
          return `npm.${packageName}.datepicker.${locale}`;
        }

        if (packageName === "date-fns") {
          // build separate bundles for dateFn locales
          // which can be included on demand in the view templates
          // or used as dynamic import and handled by webpack
          const dateFunctionLocaleMatch = id.match(/node_modules\/date-fns\/esm\/locale\/((?!en)(?!_)\w\w)/);
          if (dateFunctionLocaleMatch) {
            const locale = dateFunctionLocaleMatch[1];
            return `npm.${packageName}.${locale}`;
          }
        }

        // npm package names are URL-safe, but some servers don't like @ symbols
        return `npm.${packageName.replace("@", "")}`;
      }
    },
  },
  moduleContext(id) {
    if (id.includes("@duetds")) {
      return "window";
    }
  },
  plugins: [
    {
      name: "clean",
      buildStart() {
        rimraf.sync(paths.dist);
      },
    },
    replace({
      preventAssignment: true,
      "process.env.NODE_ENV": JSON.stringify(NODE_ENV),
      "process.env.MODE": JSON.stringify(MODE),
    }),
    inject({
      $: "jquery",
      jQuery: "jquery",
    }),
    postcss({
      use: ["less"],
      extract: "css/common.css",
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
      // pin target to explicitly support older browser versions
      // for browser support of $feature see https://caniuse.com/?search=es2022
      target: "es2022",
      sourceMap: true,
      minify: isProduction,
    }),
    assetsManifest({
      output: "target/classes/assets-manifest.json",
      publicPath: "/assets",
    }),
  ],
};

function inputFiles() {
  const files = glob.sync(`${paths.src}/bundles/*.js`);
  return Object.fromEntries(files.map((file) => [entryName(file), file]));
}

function entryName(file) {
  const filename = file.slice(Math.max(0, file.lastIndexOf("/") + 1));
  return filename.slice(0, Math.max(0, filename.lastIndexOf("."))).replace(/-/g, "_");
}
