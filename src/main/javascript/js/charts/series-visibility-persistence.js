/**
 * @typedef {{ name: string, [key: string]: * }} ApexChartSeriesItem
 */

const STORAGE_PREFIX = "uv:chart-series-visibility";

/**
 * Enhances the given apex chart options with persistence of visible data rows.
 *
 * @param {ApexChart.ApexOptions} apexOptions apex chart options
 * @param {{ version: Date, key: string, getId: (entry: ApexChartSeriesItem) => string }} options persistence related options
 * @return {*} enhanced apex options
 */
export function apexOptionsWithPersistence(apexOptions, options) {
  const hiddenIds = loadHiddenIds(options.key, options.version);
  const ids = apexOptions.series.map((entry) => options.getId(entry));

  const series = apexOptions.series.map((entry, index) => ({
    ...entry,
    hidden: hiddenIds.has(ids[index]),
  }));

  function toggleVisibility(id) {
    if (id) {
      if (hiddenIds.has(id)) {
        hiddenIds.delete(id);
      } else {
        hiddenIds.add(id);
      }
      saveHiddenIds(options.key, options.version, hiddenIds);
    }
  }

  return {
    ...apexOptions,
    chart: {
      ...apexOptions.chart,
      events: {
        ...apexOptions.chart?.events,
        legendClick: function (chartContext, seriesIndex) {
          // ids is int the same order as data-series.
          // therefore the index returns the matching id.
          toggleVisibility(ids[seriesIndex]);

          apexOptions.chart?.events?.legendClick?.(chartContext, seriesIndex);
        },
      },
    },
    series,
  };
}

/**
 *
 * @param {string} storageKey
 * @return {string}
 */
function buildStorageKey(storageKey) {
  return `${STORAGE_PREFIX}:${globalThis.uv.userId}:${storageKey}`;
}

/**
 *
 * @param {string} storageKey storage prefix key
 * @param {Date} version version of the persisted data
 * @return {Set<string>}
 */
function loadHiddenIds(storageKey, version) {
  const key = buildStorageKey(storageKey);
  try {
    const raw = globalThis.localStorage.getItem(key);
    const parsed = raw ? JSON.parse(raw) : undefined;
    const persistedVersion = parsed?.version ? new Date(parsed.version) : undefined;
    if (!persistedVersion || version.getTime() !== persistedVersion.getTime()) {
      // no entry yet, or version differs from persisted -> use default - everything visible
      globalThis.localStorage.removeItem(key);
      return new Set();
    }
    return new Set(Array.isArray(parsed?.hiddenIds) ? parsed.hiddenIds : []);
  } catch {
    // corrupt entry, unavailable storage (e.g. private mode) - fall back to "nothing hidden".
    return new Set();
  }
}

/**
 *
 * @param {string} storageKey
 * @param {Date} version
 * @param {Set[string]} hiddenIds
 */
function saveHiddenIds(storageKey, version, hiddenIds) {
  try {
    globalThis.localStorage.setItem(
      buildStorageKey(storageKey),
      JSON.stringify({ version, hiddenIds: [...hiddenIds] }),
    );
  } catch {
    // storage unavailable/full - visibility just won't persist across reloads.
  }
}
