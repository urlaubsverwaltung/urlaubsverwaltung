import rimraf from "rimraf";

/**
 *
 * @param options
 * @param options.path {string}
 * @returns {{name: string, buildStart(): void}}
 */
export default function clean(options) {
  return {
    name: "clean",
    buildStart() {
      rimraf.sync(options.path);
    },
  };
}
