import fs from "node:fs";

/**
 *
 * @param output {string} filename of the generated assets manifest
 * @param publicPath {string} public path prefix for assets
 * @param [key] {(id, entry) => string|undefined} optional function to change the key of the asset
 * @returns {{generateBundle(*, *): void, name: string}}
 */
export default function assetManifest({ output, publicPath, key = () => {} }) {
  return {
    name: "generate-manifest",
    generateBundle(options, bundle) {
      const bundledEntryPoints = Object.entries(bundle)
        .map(([id, entry]) => {
          if (entry.isEntry || entry.type === "chunk") {
            return [key(id, entry) ?? `${entry.name}.js`, `${publicPath}/${entry.fileName}`];
          }
          if (entry.type === "asset") {
            return [key(id, entry) ?? entry.name, `${publicPath}/${entry.fileName}`];
          }
          if (entry.type === "chunk") {
            // note: rollup generates multiple `index-$hash.js` chunks.
            //       we are generating out assets-manifest with only one `index.js`, however.
            //       this is fine, as long as we don't want to preload these chunks.
            return [key(id, entry) ?? `${entry.name}.js`, `${publicPath}/${entry.fileName}`];
          }
        })
        .filter(Boolean);

      // sort keys alphabetically
      bundledEntryPoints.sort(([nameA], [nameB]) => nameA.localeCompare(nameB));

      fs.writeFileSync(output, JSON.stringify(Object.fromEntries(bundledEntryPoints), undefined, 2), "utf8");
    },
  };
}
