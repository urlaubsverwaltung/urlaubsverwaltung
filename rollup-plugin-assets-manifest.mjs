import fs from "node:fs";

/**
 *
 * @param output {string} filename of the generated assets manifest
 * @param publicPath {string} public path prefix for assets
 * @returns {{generateBundle(*, *): void, name: string}}
 */
export function assetsManifest({ output, publicPath}) {
  function url(dep) {
    return `${publicPath}/${dep}`;
  }

  return {
    name: "generate-manifest",
    generateBundle(options, bundle) {
      const bundleEntries = Object.entries(bundle);

      function indexOfKey(key) {
        return bundleEntries.findIndex(([k]) => k === key);
      }

      function getImportsOf(dep) {
        const [, entry] = bundleEntries[indexOfKey(dep)];
        return entry ? [...entry.imports.map((d) => url(d)), ...entry.imports.flatMap((d) => getImportsOf(d))] : [];
      }

      function transitiveDependencies(id) {
        return [...new Set(getImportsOf(id))];
      }

      const bundledEntryPoints = Object.entries(bundle)
        .map(([id, entry]) => {
          if (entry.isEntry) {
            const keyName = `${entry.name}.js`;
            const dependencies = transitiveDependencies(id);
            return [
              keyName,
              {
                url: `${publicPath}/${entry.fileName}`,
                dependencies,
              },
            ];
          }
          if (entry.type === "asset") {
            // since rollup v3 sourcemaps are exposed as "asset". sourcemaps do not have a name, however.
            // and I don't think sourcemaps have to be added to the manifest file.
            const keyName = entry.name;
            if (keyName) {
              return [
                keyName,
                {
                  url: `${publicPath}/${entry.fileName}`,
                  dependencies: [],
                },
              ];
            }
          }
        })
        .filter(Boolean);

      // sort keys alphabetically
      bundledEntryPoints.sort(([nameA], [nameB]) => nameA.localeCompare(nameB));

      fs.writeFileSync(output, JSON.stringify(Object.fromEntries(bundledEntryPoints), undefined, 2), "utf8");
    },
  };
}
