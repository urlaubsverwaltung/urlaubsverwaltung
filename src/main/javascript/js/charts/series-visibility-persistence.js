/**
 * @typedef {{ id: string, hidden?: boolean, [key: string]: * }} SeriesWithId
 */

const STORAGE_PREFIX = "uv:series-visibility";

/**
 * Enhances the given apex chart options with persistence of visible data rows.
 *
 * @param {ApexChart.ApexOptions} apexOptions apex chart options
 * @param {{ version: number, key: string, getId: (entry) => string }} options persistence related options
 * @return {ApexChart.ApexOptions} enhanced apex options
 */
export function apexOptionsWithPersistence(apexOptions, options) {
  const hiddenIds = loadHiddenIds(options.key);
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
      saveHiddenIds(options.key, hiddenIds);
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

function buildStorageKey(storageKey) {
  return `${STORAGE_PREFIX}:${globalThis.uv.loggedInPersonId}:${storageKey}`;
}

function loadHiddenIds(storageKey) {
  try {
    const raw = globalThis.localStorage.getItem(buildStorageKey(storageKey));
    const parsed = raw ? JSON.parse(raw) : undefined;
    return new Set(Array.isArray(parsed?.hiddenIds) ? parsed.hiddenIds : []);
  } catch {
    // corrupt entry, unavailable storage (e.g. private mode) - fall back to "nothing hidden".
    return new Set();
  }
}

function saveHiddenIds(storageKey, hiddenIds) {
  try {
    globalThis.localStorage.setItem(buildStorageKey(storageKey), JSON.stringify({ hiddenIds: [...hiddenIds] }));
  } catch {
    // storage unavailable/full - visibility just won't persist across reloads.
  }
}
