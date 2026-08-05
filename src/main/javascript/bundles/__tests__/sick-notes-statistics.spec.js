import { readFileSync } from "node:fs";
import { observable } from "../../js/observable";

vi.mock("../../js/common", () => ({}));
vi.mock("apexcharts/radialBar", () => ({}));
vi.mock("apexcharts/bar", () => ({}));
vi.mock("apexcharts/line", () => ({}));
vi.mock("apexcharts/features/legend", () => ({}));
vi.mock("apexcharts/features/keyboard", () => ({}));

describe("sick-notes-statistics", function () {
  let chartInstances;
  let MockApexCharts;
  let themeObservable;
  let reducedMotionObservable;

  beforeEach(function () {
    vi.resetModules();

    globalThis.localStorage.clear();
    globalThis.uv = { userId: "user-1" };

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
      <div id="sicknote-statistic-sick-rate-chart"></div>
      <div id="sicknote-statistic-verteilung"></div>
    `;
  });

  afterEach(function () {
    document.body.innerHTML = "";
    delete globalThis.sicknoteStatistic;
    delete globalThis.uv;
    globalThis.localStorage.clear();
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
      sickRateYaxisTitle: "Krankenquote in %",
      sickRateValues: [
        { year: 2024, data: [10, 12, 14] },
        { year: 2023, data: [8, 9, 10] },
      ],
      dataseriesValuesForAtLeastOneSickNotePercent: [42, 37],
      ...overrides,
    };
  }

  async function loadModule() {
    return import("../sick-notes-statistics.js");
  }

  // the bundle constructs the charts in this order
  function barChart() {
    return chartInstances[0];
  }

  function sickRateChart() {
    return chartInstances[1];
  }

  function radialChart() {
    return chartInstances[2];
  }

  describe("main sick-note chart", function () {
    it("builds grouped series: previous-year series first, then current-year series", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = barChart();
      expect(options.series).toEqual([
        { name: "Krank 2023", group: "previousYear", data: [1, 2, 3], hidden: false },
        { name: "Kind krank 2023", group: "previousYear", data: [4, 5, 6], hidden: false },
        { name: "Krank 2024", group: "currentYear", data: [7, 8, 9], hidden: false },
        { name: "Kind krank 2024", group: "currentYear", data: [10, 11, 12], hidden: false },
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

      const { options } = barChart();
      expect(options.series).toEqual([
        { name: "Krank 2023", group: "previousYear", data: [1, 2, 3], hidden: false },
        { name: "Kind krank 2023", group: "previousYear", data: [4, 5, 6], hidden: false },
        { name: "Krank 2024", group: "currentYear", data: [7, 8, 9], hidden: false },
        { name: "Kind krank 2024", group: "currentYear", data: [10, 11, 12], hidden: false },
      ]);
    });

    it("leaves the sick rate to its own chart", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = barChart();
      // the four day series only - no rate line, and therefore no second (percentage) y-axis
      expect(options.series).toHaveLength(4);
      expect(options.yaxis).not.toBeInstanceOf(Array);
    });

    it("renders into #sicknote-statistic-chart and calls render", async function () {
      setSicknoteStatistic();
      await loadModule();

      expect(barChart().element).toBe(document.querySelector("#sicknote-statistic-chart"));
      expect(barChart().render).toHaveBeenCalled();
    });

    it("configures a stacked bar chart with hidden toolbar and top-right legend", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = barChart();
      expect(options.chart.type).toBe("bar");
      expect(options.chart.stacked).toBe(true);
      expect(options.chart.toolbar.show).toBe(false);
      expect(options.legend).toEqual({ position: "top", horizontalAlign: "right" });
    });

    it("uses the backend-provided x-axis categories and y-axis title", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = barChart();
      expect(options.xaxis.categories).toEqual(["Jan", "Feb", "Mar"]);
      expect(options.yaxis.title.text).toBe("Tage");
    });

    it("orders colors as [previousYearSick, previousYearChildSick, currentYearSick, currentYearChildSick]", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = barChart();
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

      const { options } = barChart();
      expect(options.states.hover.filter.type).toBe("none");
      expect(options.states.active.filter.type).toBe("none");
    });

    it("configures the tooltip as shared, non-intersecting and cursor-following", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = barChart();
      expect(options.tooltip.shared).toBe(true);
      expect(options.tooltip.intersect).toBe(false);
      expect(options.tooltip.followCursor).toBe(true);
    });
  });

  describe("sick rate chart", function () {
    // apexcharts hands over the month index in w.globals.labels here (numeric x-axis), which is
    // exactly what the tooltip must not use
    function callSickRateTooltip(seriesValues, dataPointIndex) {
      return sickRateChart().options.tooltip.custom({
        series: seriesValues,
        dataPointIndex,
        w: {
          globals: { labels: [1, 2, 3], seriesNames: ["2024", "2023"] },
          config: { colors: ["rate", "rate-light"] },
        },
      });
    }

    it("renders into #sicknote-statistic-sick-rate-chart and calls render", async function () {
      setSicknoteStatistic();
      await loadModule();

      expect(sickRateChart().element).toBe(document.querySelector("#sicknote-statistic-sick-rate-chart"));
      expect(sickRateChart().render).toHaveBeenCalled();
    });

    it("draws one line per year, current year first", async function () {
      setSicknoteStatistic();
      await loadModule();

      expect(sickRateChart().options.chart.type).toBe("line");
      expect(sickRateChart().options.series).toEqual([
        { name: "2024", data: [10, 12, 14], hidden: false },
        { name: "2023", data: [8, 9, 10], hidden: false },
      ]);
    });

    it("shares the months of the bar chart and labels its own axis as a percentage", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = sickRateChart();
      expect(options.xaxis.categories).toEqual(["Jan", "Feb", "Mar"]);
      expect(options.yaxis.title.text).toBe("Krankenquote in %");
      expect(options.yaxis.labels.formatter(12.34)).toBe("12.3%");
    });

    it("gives a year the same color it has in the days chart", async function () {
      setSicknoteStatistic();
      await loadModule();

      const [currentYearDays, previousYearDays] = [barChart().options.colors[2], barChart().options.colors[0]];
      expect(sickRateChart().options.colors).toEqual([currentYearDays, previousYearDays]);
    });

    it("does not label every single point", async function () {
      setSicknoteStatistic();
      await loadModule();

      expect(sickRateChart().options.dataLabels.enabled).toBe(false);
    });

    it("shows the current year with the difference to the previous year, the previous year plain", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callSickRateTooltip(
        [
          [9, 12],
          [8, 10],
        ],
        1,
      );

      expect(html).toContain("2024: 12% (+2% / +20%)");
      expect(html).toContain("2023: 10%");
      expect(html).not.toContain("2023: 10% (");
    });

    // apexcharts infers a numeric x-axis for a line chart, so w.globals.labels holds the month
    // index rather than its name - the title has to come from the model instead
    it("titles the tooltip with the month name, not its index", async function () {
      setSicknoteStatistic();
      await loadModule();

      const html = callSickRateTooltip(
        [
          [9, 12],
          [8, 10],
        ],
        1,
      );

      expect(html).toContain('<div class="sicknote-statistics-tooltip-title">Feb</div>');
    });

    it("restores a line the user hid before the reload", async function () {
      globalThis.localStorage.setItem(
        "uv:chart-series-visibility:user-1:sicknote-statistics-sick-rate",
        JSON.stringify({ version: new Date("2026-08-05").toISOString(), hiddenIds: ["sick-rate-compare"] }),
      );

      setSicknoteStatistic();
      await loadModule();

      const { series } = sickRateChart().options;
      expect(series[0].hidden).toBe(false);
      expect(series[1].hidden).toBe(true);
    });
  });

  describe("tooltip custom rendering", function () {
    function callTooltip(seriesValues, dataPointIndex = 0) {
      return barChart().options.tooltip.custom({
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
    it("uses dark theme mode for every chart when the current theme is dark", async function () {
      setSicknoteStatistic();
      themeObservable.value = "dark";
      await loadModule();

      for (const { options } of chartInstances) {
        expect(options.theme.mode).toBe("dark");
      }
    });

    it("uses light theme mode for every chart when the current theme is not dark", async function () {
      setSicknoteStatistic();
      themeObservable.value = "light";
      await loadModule();

      for (const { options } of chartInstances) {
        expect(options.theme.mode).toBe("light");
      }
    });

    it("updates every chart's theme mode when the theme changes afterwards", async function () {
      setSicknoteStatistic();
      await loadModule();

      themeObservable.value = "dark";
      await Promise.resolve();

      for (const { updateOptions } of chartInstances) {
        expect(updateOptions).toHaveBeenCalledWith({ theme: { mode: "dark" } });
      }
    });
  });

  describe("reduced motion handling", function () {
    it("disables animations for every chart when reduced motion is preferred", async function () {
      setSicknoteStatistic();
      reducedMotionObservable.value = true;
      await loadModule();

      for (const { options } of chartInstances) {
        expect(options.chart.animations.enabled).toBe(false);
      }
    });

    it("enables animations for every chart when reduced motion is not preferred", async function () {
      setSicknoteStatistic();
      reducedMotionObservable.value = false;
      await loadModule();

      for (const { options } of chartInstances) {
        expect(options.chart.animations.enabled).toBe(true);
      }
    });
  });

  describe("at-least-one-sick-note radial chart", function () {
    it("renders into #sicknote-statistic-verteilung and calls render", async function () {
      setSicknoteStatistic();
      await loadModule();

      expect(radialChart().element).toBe(document.querySelector("#sicknote-statistic-verteilung"));
      expect(radialChart().render).toHaveBeenCalled();
    });

    it("uses the backend-provided current/previous-year percentages as its series", async function () {
      setSicknoteStatistic({ dataseriesValuesForAtLeastOneSickNotePercent: [42, 37] });
      await loadModule();

      expect(radialChart().options.series).toEqual([42, 37]);
    });

    it("falls back to [0, 0] when no percentages are provided", async function () {
      setSicknoteStatistic({ dataseriesValuesForAtLeastOneSickNotePercent: undefined });
      await loadModule();

      expect(radialChart().options.series).toEqual([0, 0]);
    });

    it("orders colors as [currentYear, previousYear]", async function () {
      setSicknoteStatistic();
      await loadModule();

      expect(radialChart().options.colors).toEqual(["var(--sick-note-color)", "var(--sick-note-color-light)"]);
    });

    it("formats bar labels as a percentage of the given series value", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { formatter } = radialChart().options.plotOptions.radialBar.barLabels;
      const result = formatter("some name", { seriesIndex: 1, w: { globals: { series: [42, 37] } } });

      expect(result).toBe("37%");
    });

    it("disables the tooltip and legend", async function () {
      setSicknoteStatistic();
      await loadModule();

      const { options } = radialChart();
      expect(options.tooltip.enabled).toBe(false);
      expect(options.legend.show).toBe(false);
    });
  });

  // the chart hosts are empty until this bundle has executed - the css has to reserve the box up
  // front, otherwise the page paints collapsed charts and reflows by their full size afterwards.
  it("reserves the chart and gauge size in the stylesheet", async function () {
    setSicknoteStatistic();
    await loadModule();

    const css = readFileSync("src/main/css/bundles/sick-note-statistics.css", "utf8");

    // both cartesian charts share the .sicknote-statistics-chart host, so one reservation covers them
    expect(barChart().options.chart.height).toBe(sickRateChart().options.chart.height);
    expect(css).toContain(`calc(${barChart().options.chart.height}px +`);

    expect(css).toContain(`width: ${radialChart().options.chart.width};`);
    expect(css).toContain(`min-height: ${radialChart().options.chart.height}px;`);
  });
});
