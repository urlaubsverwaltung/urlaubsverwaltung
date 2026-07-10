import { apexOptionsWithPersistence } from "../series-visibility-persistence";

const VERSION = new Date("2024-01-01T00:00:00Z");

describe("apexOptionsWithPersistence", () => {
  beforeEach(() => {
    globalThis.uv = { userId: "user-1" };
    localStorage.clear();
  });

  test("throws for unsupported chart types", () => {
    const apexOptions = buildOptions({ type: "pie" });

    expect(() => apexOptionsWithPersistence(apexOptions, buildPersistenceOptions())).toThrow(
      /unsupported chart\.type "pie"/,
    );
  });

  test.each(["bar", "line", "area", "scatter", "candlestick", "boxPlot", "rangeBar", "heatmap"])(
    "does not throw for supported chart type %s",
    (type) => {
      const apexOptions = buildOptions({ type });

      expect(() => apexOptionsWithPersistence(apexOptions, buildPersistenceOptions())).not.toThrow();
    },
  );

  test("marks all series visible when nothing is persisted yet", () => {
    const apexOptions = buildOptions();

    const result = apexOptionsWithPersistence(apexOptions, buildPersistenceOptions());

    expect(result.series).toEqual([
      { name: "a", hidden: false },
      { name: "b", hidden: false },
    ]);
  });

  test("marks series hidden based on persisted ids", () => {
    localStorage.setItem(
      "uv:chart-series-visibility:user-1:my-chart",
      JSON.stringify({ version: VERSION, hiddenIds: ["b"] }),
    );
    const apexOptions = buildOptions();

    const result = apexOptionsWithPersistence(apexOptions, buildPersistenceOptions());

    expect(result.series).toEqual([
      { name: "a", hidden: false },
      { name: "b", hidden: true },
    ]);
  });

  test("ignores persisted data with a different version", () => {
    localStorage.setItem(
      "uv:chart-series-visibility:user-1:my-chart",
      JSON.stringify({ version: new Date("2023-01-01T00:00:00Z"), hiddenIds: ["b"] }),
    );
    const apexOptions = buildOptions();

    const result = apexOptionsWithPersistence(apexOptions, buildPersistenceOptions());

    expect(result.series).toEqual([
      { name: "a", hidden: false },
      { name: "b", hidden: false },
    ]);
  });

  test("falls back to nothing hidden when persisted entry is corrupt", () => {
    localStorage.setItem("uv:chart-series-visibility:user-1:my-chart", "not-json");
    const apexOptions = buildOptions();

    const result = apexOptionsWithPersistence(apexOptions, buildPersistenceOptions());

    expect(result.series).toEqual([
      { name: "a", hidden: false },
      { name: "b", hidden: false },
    ]);
  });

  test("keys persisted state by userId and storage key", () => {
    const apexOptions = buildOptions();
    localStorage.setItem(
      "uv:chart-series-visibility:other-user:my-chart",
      JSON.stringify({ version: VERSION, hiddenIds: ["a", "b"] }),
    );

    const result = apexOptionsWithPersistence(apexOptions, buildPersistenceOptions());

    expect(result.series).toEqual([
      { name: "a", hidden: false },
      { name: "b", hidden: false },
    ]);
  });

  test("legendClick toggles visibility and persists it", () => {
    const apexOptions = buildOptions();
    const result = apexOptionsWithPersistence(apexOptions, buildPersistenceOptions());

    result.chart.events.legendClick({}, 1);

    const stored = JSON.parse(localStorage.getItem("uv:chart-series-visibility:user-1:my-chart"));
    expect(stored.hiddenIds).toEqual(["b"]);
  });

  test("legendClick toggles visibility off again on a second click", () => {
    const apexOptions = buildOptions();
    const result = apexOptionsWithPersistence(apexOptions, buildPersistenceOptions());

    result.chart.events.legendClick({}, 1);
    result.chart.events.legendClick({}, 1);

    const stored = JSON.parse(localStorage.getItem("uv:chart-series-visibility:user-1:my-chart"));
    expect(stored.hiddenIds).toEqual([]);
  });

  test("legendClick calls through to the original legendClick handler", () => {
    const originalLegendClick = vi.fn();
    const apexOptions = { ...buildOptions(), chart: { type: "bar", events: { legendClick: originalLegendClick } } };

    const result = apexOptionsWithPersistence(apexOptions, buildPersistenceOptions());
    const chartContext = {};
    result.chart.events.legendClick(chartContext, 0);

    expect(originalLegendClick).toHaveBeenCalledWith(chartContext, 0);
  });

  test("preserves other chart and top-level options", () => {
    const apexOptions = { ...buildOptions(), chart: { type: "bar", height: 300 }, title: { text: "My Chart" } };

    const result = apexOptionsWithPersistence(apexOptions, buildPersistenceOptions());

    expect(result.chart.height).toBe(300);
    expect(result.title).toEqual({ text: "My Chart" });
  });
});

function buildOptions({ type = "bar", series = [{ name: "a" }, { name: "b" }] } = {}) {
  return {
    chart: { type },
    series,
  };
}

function buildPersistenceOptions(overrides = {}) {
  return {
    version: VERSION,
    key: "my-chart",
    getId: (entry) => entry.name,
    ...overrides,
  };
}
