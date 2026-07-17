import { observable } from "../../js/observable";

vi.mock("../../js/common", () => ({}));
vi.mock("apexcharts/radialBar", () => ({}));
vi.mock("apexcharts/bar", () => ({}));
vi.mock("apexcharts/features/legend", () => ({}));
vi.mock("apexcharts/features/keyboard", () => ({}));

describe("sick-notes-statistics", function () {
  let chartInstances;
  let MockApexCharts;
  let themeObservable;
  let reducedMotionObservable;

  beforeEach(function () {
    vi.resetModules();

    chartInstances = [];
    MockApexCharts = vi.fn().mockImplementation(function (element, options) {
      this.element = element;
      this.options = options;
      this.render = vi.fn();
      this.updateOptions = vi.fn().mockResolvedValue();
      chartInstances.push(this);
    });

    themeObservable = observable("light");
    reducedMotionObservable = observable(false);

    vi.doMock("apexcharts/core", () => ({ default: MockApexCharts }));
    vi.doMock("../../js/use-theme", () => ({ useTheme: () => ({ theme: themeObservable }) }));
    vi.doMock("../../js/use-media", () => ({ useMedia: () => ({ matches: reducedMotionObservable }) }));

    document.body.innerHTML = `
      <div id="sicknote-statistic-chart"></div>
      <div id="sicknote-statistic-verteilung"></div>
    `;
  });

  afterEach(function () {
    document.body.innerHTML = "";
    delete globalThis.sicknoteStatistic;
  });

  function setSicknoteStatistic(overrides) {
    globalThis.sicknoteStatistic = {
      dataseriesNames: ["Krank", "Kind krank"],
      dataseriesValues: [
        { year: 2023, data: [1, 2, 3] },
        { year: 2023, data: [4, 5, 6] },
        { year: 2024, data: [7, 8, 9] },
        { year: 2024, data: [10, 11, 12] },
      ],
      xaxisLabels: ["Jan", "Feb", "Mar"],
      yaxisTitle: "Tage",
      dataseriesValuesForAtLeastOneSickNotePercent: [42, 37],
      ...overrides,
    };
  }

  async function loadModule() {
    return import("../sick-notes-statistics.js");
  }

  describe("main sick-note chart", function () {
    it("builds grouped series: previous-year series first, then current-year series", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = chartInstances[0];
      expect(options.series).toEqual([
        { name: "Krank 2023", group: "previousYear", data: [1, 2, 3] },
        { name: "Kind krank 2023", group: "previousYear", data: [4, 5, 6] },
        { name: "Krank 2024", group: "currentYear", data: [7, 8, 9] },
        { name: "Kind krank 2024", group: "currentYear", data: [10, 11, 12] },
      ]);
    });

    it("pairs categories by index modulo the category count regardless of input order", async function () {
      setSicknoteStatistic({
        dataseriesValues: [
          { year: 2024, data: [7, 8, 9] },
          { year: 2024, data: [10, 11, 12] },
          { year: 2023, data: [1, 2, 3] },
          { year: 2023, data: [4, 5, 6] },
        ],
      });
      await loadModule();

      const { options } = chartInstances[0];
      expect(options.series).toEqual([
        { name: "Krank 2023", group: "previousYear", data: [1, 2, 3] },
        { name: "Kind krank 2023", group: "previousYear", data: [4, 5, 6] },
        { name: "Krank 2024", group: "currentYear", data: [7, 8, 9] },
        { name: "Kind krank 2024", group: "currentYear", data: [10, 11, 12] },
      ]);
    });

    it("renders into #sicknote-statistic-chart and calls render", async function () {
      setSicknoteStatistic();
      await loadModule();

      expect(chartInstances[0].element).toBe(document.querySelector("#sicknote-statistic-chart"));
      expect(chartInstances[0].render).toHaveBeenCalled();
    });

    it("configures a stacked bar chart with hidden toolbar and top-right legend", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = chartInstances[0];
      expect(options.chart.type).toBe("bar");
      expect(options.chart.stacked).toBe(true);
      expect(options.chart.toolbar.show).toBe(false);
      expect(options.legend).toEqual({ position: "top", horizontalAlign: "right" });
    });

    it("uses the backend-provided x-axis categories and y-axis title", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = chartInstances[0];
      expect(options.xaxis.categories).toEqual(["Jan", "Feb", "Mar"]);
      expect(options.yaxis.title.text).toBe("Tage");
    });

    it("orders colors as [previousYearSick, previousYearChildSick, currentYearSick, currentYearChildSick]", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = chartInstances[0];
      expect(options.colors).toEqual([
        "var(--sick-note-color-light)",
        "var(--sick-note-child-color-light)",
        "var(--sick-note-color)",
        "var(--sick-note-child-color)",
      ]);
    });

    it("disables the hover/active state filter so bars don't dim on hover", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = chartInstances[0];
      expect(options.states.hover.filter.type).toBe("none");
      expect(options.states.active.filter.type).toBe("none");
    });

    it("configures the tooltip as shared, non-intersecting and cursor-following", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = chartInstances[0];
      expect(options.tooltip.shared).toBe(true);
      expect(options.tooltip.intersect).toBe(false);
      expect(options.tooltip.followCursor).toBe(true);
    });
  });

  describe("tooltip custom rendering", function () {
    function callTooltip(seriesValues, dataPointIndex = 0) {
      return chartInstances[0].options.tooltip.custom({
        series: seriesValues,
        dataPointIndex,
        w: {
          globals: {
            labels: ["Jan", "Feb", "Mar"],
            seriesNames: ["Krank 2023", "Kind krank 2023", "Krank 2024", "Kind krank 2024"],
          },
          config: {
            colors: ["light-sick", "light-child", "sick", "child"],
          },
        },
      });
    }

    it("shows the category label as the tooltip title", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callTooltip([[10], [5], [12], [6]], 0);
      expect(html).toContain('<div class="sicknote-statistics-tooltip-title">Jan</div>');
    });

    it("orders rows as current/previous pairs: sick then child-sick", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callTooltip([[10], [5], [12], [6]], 0);
      const sickCurrentIndex = html.indexOf("Krank 2024");
      const sickPreviousIndex = html.indexOf("Krank 2023");
      const childCurrentIndex = html.indexOf("Kind krank 2024");
      const childPreviousIndex = html.indexOf("Kind krank 2023");

      expect(sickCurrentIndex).toBeGreaterThanOrEqual(0);
      expect(sickCurrentIndex).toBeLessThan(sickPreviousIndex);
      expect(sickPreviousIndex).toBeLessThan(childCurrentIndex);
      expect(childCurrentIndex).toBeLessThan(childPreviousIndex);
    });

    it("shows previous-year rows as the plain rounded value with no comparison", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callTooltip([[10], [5], [12], [6]], 0);
      expect(html).toContain("Krank 2023: 10");
      expect(html).toContain("Kind krank 2023: 5");
    });

    it("shows current-year rows with an absolute and percentage difference for a positive change", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callTooltip([[10], [5], [12], [6]], 0);
      // sick: 12 vs 10 -> +2 / +20%
      expect(html).toContain("Krank 2024: 12 (+2 / +20%)");
      // child-sick: 6 vs 5 -> +1 / +20%
      expect(html).toContain("Kind krank 2024: 6 (+1 / +20%)");
    });

    it("shows a negative change without a leading plus sign", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callTooltip([[10], [5], [8], [6]], 0);
      // sick: 8 vs 10 -> -2 / -20%
      expect(html).toContain("Krank 2024: 8 (-2 / -20%)");
    });

    it("omits the percentage when the previous year's value was zero", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callTooltip([[0], [5], [3], [6]], 0);
      expect(html).toContain("Krank 2024: 3 (+3)");
      expect(html).not.toContain("Krank 2024: 3 (+3 /");
    });

    it("shows only the rounded value when there is no comparable previous-year value", async function () {
      setSicknoteStatistic();
      await loadModule();

      // seriesValues[0] (previous-year sick) has no entry at dataPointIndex 0
      const html = callTooltip([[], [5], [12], [6]], 0);
      expect(html).toContain("Krank 2024: 12");
      expect(html).not.toContain("Krank 2024: 12 (");
    });

    it("rounds values to one decimal place", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callTooltip([[10.36], [5], [12.34], [6]], 0);
      expect(html).toContain("Krank 2023: 10.4");
      expect(html).toContain("Krank 2024: 12.3");
    });

    it("includes a color swatch per row using the series color", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callTooltip([[10], [5], [12], [6]], 0);
      expect(html).toContain('style="background-color: sick"');
      expect(html).toContain('style="background-color: light-sick"');
    });
  });

  describe("theme handling", function () {
    it("uses dark theme mode for both charts when the current theme is dark", async function () {
      setSicknoteStatistic();
      themeObservable.value = "dark";
      await loadModule();

      expect(chartInstances[0].options.theme.mode).toBe("dark");
      expect(chartInstances[1].options.theme.mode).toBe("dark");
    });

    it("uses light theme mode for both charts when the current theme is not dark", async function () {
      setSicknoteStatistic();
      themeObservable.value = "light";
      await loadModule();

      expect(chartInstances[0].options.theme.mode).toBe("light");
      expect(chartInstances[1].options.theme.mode).toBe("light");
    });

    it("updates both charts' theme mode when the theme changes afterwards", async function () {
      setSicknoteStatistic();
      await loadModule();

      themeObservable.value = "dark";
      await Promise.resolve();

      expect(chartInstances[0].updateOptions).toHaveBeenCalledWith({ theme: { mode: "dark" } });
      expect(chartInstances[1].updateOptions).toHaveBeenCalledWith({ theme: { mode: "dark" } });
    });
  });

  describe("reduced motion handling", function () {
    it("disables chart animations for both charts when reduced motion is preferred", async function () {
      setSicknoteStatistic();
      reducedMotionObservable.value = true;
      await loadModule();

      expect(chartInstances[0].options.chart.animations.enabled).toBe(false);
      expect(chartInstances[1].options.chart.animations.enabled).toBe(false);
    });

    it("enables chart animations for both charts when reduced motion is not preferred", async function () {
      setSicknoteStatistic();
      reducedMotionObservable.value = false;
      await loadModule();

      expect(chartInstances[0].options.chart.animations.enabled).toBe(true);
      expect(chartInstances[1].options.chart.animations.enabled).toBe(true);
    });
  });

  describe("at-least-one-sick-note radial chart", function () {
    it("renders into #sicknote-statistic-verteilung and calls render", async function () {
      setSicknoteStatistic();
      await loadModule();

      expect(chartInstances[1].element).toBe(document.querySelector("#sicknote-statistic-verteilung"));
      expect(chartInstances[1].render).toHaveBeenCalled();
    });

    it("uses the backend-provided current/previous-year percentages as its series", async function () {
      setSicknoteStatistic({ dataseriesValuesForAtLeastOneSickNotePercent: [42, 37] });
      await loadModule();

      expect(chartInstances[1].options.series).toEqual([42, 37]);
    });

    it("falls back to [0, 0] when no percentages are provided", async function () {
      setSicknoteStatistic({ dataseriesValuesForAtLeastOneSickNotePercent: undefined });
      await loadModule();

      expect(chartInstances[1].options.series).toEqual([0, 0]);
    });

    it("orders colors as [currentYear, previousYear]", async function () {
      setSicknoteStatistic();
      await loadModule();

      expect(chartInstances[1].options.colors).toEqual(["var(--sick-note-color)", "var(--sick-note-color-light)"]);
    });

    it("formats bar labels as a percentage of the given series value", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { formatter } = chartInstances[1].options.plotOptions.radialBar.barLabels;
      const result = formatter("some name", { seriesIndex: 1, w: { globals: { series: [42, 37] } } });

      expect(result).toBe("37%");
    });

    it("disables the tooltip and legend", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = chartInstances[1];
      expect(options.tooltip.enabled).toBe(false);
      expect(options.legend.show).toBe(false);
    });
  });
});
