import { replacePlugin } from "rolldown/plugins";
import { dynamicImportVarsPlugin } from "rolldown/experimental";
import { assetsManifest } from "./rollup-plugin-assets-manifest.mjs";
import { css } from "./rollup-plugin-css.mjs";
import glob from "fast-glob";

const NODE_ENV = process.env.NODE_ENV;
const MODE = process.env.MODE || NODE_ENV || "development";
const isProduction = MODE === "production";

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
    codeSplitting: {
      groups: [
        {
          name: "duet-date-picker",
          test: /node_modules[\\/]@duetds/,
        },
        {
          name: "date-fns",
          test: /node_modules[\\/]date-fns/,
        },
        {
          name(moduleId) {
            if (moduleId.includes("node_modules")) {
              const packageName = moduleId.match(/[/\\]node_modules[/\\](.*?)([/\\]|$)/)[1];
              // npm package names are URL-safe, but some servers don't like @ symbols
              return `npm.${packageName.replace("@", "")}`;
            }
          },
        },
      ],
    },
  },
  treeshake: {
    moduleSideEffects: [{ test: /\.css$/, sideEffects: true }],
  },
  // moduleContext(id) {
  //   if (id.includes("@duetds")) {
  //     return "window";
  //   }
  // },
  plugins: [
    replacePlugin(
      {
        "process.env.NODE_ENV": JSON.stringify(NODE_ENV),
        "process.env.MODE": JSON.stringify(MODE),
      },
      { preventAssignment: true },
    ),
    // css({
    //   extract: `${paths.dist}/../css/common.css`,
    // }),
    // dynamicImportVarsPlugin is required for duetds-datepicker (bundled with stencil and dynamic imports)
    dynamicImportVarsPlugin(),
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
  return filename.slice(0, Math.max(0, filename.lastIndexOf("."))).replaceAll("-", "_");
}
