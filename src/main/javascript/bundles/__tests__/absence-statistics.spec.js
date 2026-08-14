import { readFileSync } from "node:fs";
import { observable } from "../../js/observable";

vi.mock("../../js/common", () => ({}));
vi.mock("apexcharts/radialBar", () => ({}));
vi.mock("apexcharts/bar", () => ({}));
vi.mock("apexcharts/pie", () => ({}));
vi.mock("apexcharts/features/legend", () => ({}));
vi.mock("apexcharts/features/keyboard", () => ({}));

describe("absence-statistics", function () {
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

    document.documentElement.lang = "de-DE";
    document.body.innerHTML = `
      <div id="monthly-chart"></div>
      <div id="distribution-chart"></div>
      <div id="vacation-ring"></div>
    `;
  });

  afterEach(function () {
    document.body.innerHTML = "";
    delete globalThis.absenceStatistics;
  });

  function setAbsenceStatistics(overrides) {
    globalThis.absenceStatistics = {
      xaxisLabels: ["Jan", "Feb", "Mar"],
      yaxisTitle: "Tage",
      types: [
        { name: "Erholungsurlaub", color: "YELLOW", monthlyDays: [5, 0, 3], yearSum: 8, share: 66.7, active: true },
        { name: "Sonderurlaub", color: "VIOLET", monthlyDays: [0, 4, 0], yearSum: 4, share: 33.3, active: true },
      ],
      vacationDaysTakenPercentage: 67,
      ...overrides,
    };
  }

  async function loadModule() {
    return import("../absence-statistics.js");
  }

  // the bundle constructs the charts in this order
  function monthlyChart() {
    return chartInstances[0];
  }

  function distributionChart() {
    return chartInstances[1];
  }

  function ringChart() {
    return chartInstances[2];
  }

  describe("monthly chart", function () {
    it("builds one series per type, in model order", async function () {
      setAbsenceStatistics();
      await loadModule();

      expect(monthlyChart().options.series).toEqual([
        { name: "Erholungsurlaub", data: [5, 0, 3] },
        { name: "Sonderurlaub", data: [0, 4, 0] },
      ]);
    });

    it("uses the model's colors, in model order", async function () {
      setAbsenceStatistics();
      await loadModule();

      expect(monthlyChart().options.colors).toEqual(["var(--absence-color-YELLOW)", "var(--absence-color-VIOLET)"]);
    });

    it("renders into #monthly-chart and calls render", async function () {
      setAbsenceStatistics();
      await loadModule();

      expect(monthlyChart().element).toBe(document.querySelector("#monthly-chart"));
      expect(monthlyChart().render).toHaveBeenCalled();
    });

    it("configures a stacked bar chart with hidden toolbar and disabled data labels", async function () {
      setAbsenceStatistics();
      await loadModule();

      const { options } = monthlyChart();
      expect(options.chart.type).toBe("bar");
      expect(options.chart.stacked).toBe(true);
      expect(options.chart.toolbar.show).toBe(false);
      expect(options.dataLabels.enabled).toBe(false);
    });

    it("uses the backend-provided x-axis categories and y-axis title", async function () {
      setAbsenceStatistics();
      await loadModule();

      const { options } = monthlyChart();
      expect(options.xaxis.categories).toEqual(["Jan", "Feb", "Mar"]);
      expect(options.yaxis.title.text).toBe("Tage");
    });

    it("adds a 2px transparent stroke to separate stacked segments", async function () {
      setAbsenceStatistics();
      await loadModule();

      expect(monthlyChart().options.stroke).toEqual({ show: true, width: 2, colors: ["transparent"] });
    });

    it("disables the hover/active state filter so segments don't dim on hover", async function () {
      setAbsenceStatistics();
      await loadModule();

      const { states } = monthlyChart().options;
      expect(states.hover.filter.type).toBe("none");
      expect(states.active.filter.type).toBe("none");
    });
  });

  describe("monthly chart tooltip", function () {
    function callTooltip(seriesValues, dataPointIndex = 0) {
      return monthlyChart().options.tooltip.custom({
        series: seriesValues,
        dataPointIndex,
        w: {
          globals: {
            seriesNames: ["Erholungsurlaub", "Sonderurlaub"],
          },
          config: {
            colors: ["yellow", "violet"],
          },
        },
      });
    }

    it("titles the tooltip with the month name, not its index", async function () {
      setAbsenceStatistics();
      await loadModule();

      const html = callTooltip(
        [
          [5, 0, 3],
          [0, 4, 0],
        ],
        1,
      );
      expect(html).toContain('<div class="absence-statistics-tooltip-title">Feb</div>');
    });

    it("shows one row per type, sorted descending by that month's value", async function () {
      setAbsenceStatistics();
      await loadModule();

      const html = callTooltip([[3], [7]], 0);
      const smallIndex = html.indexOf("Erholungsurlaub");
      const bigIndex = html.indexOf("Sonderurlaub");

      expect(bigIndex).toBeGreaterThanOrEqual(0);
      expect(bigIndex).toBeLessThan(smallIndex);
      expect(html).toContain("Erholungsurlaub: 3 Tage");
      expect(html).toContain("Sonderurlaub: 7 Tage");
    });

    it("omits a type without a single day in that month", async function () {
      setAbsenceStatistics();
      await loadModule();

      const html = callTooltip([[5], [0]], 0);
      expect(html).toContain("Erholungsurlaub");
      expect(html).not.toContain("Sonderurlaub");
    });

    it("includes the month total", async function () {
      setAbsenceStatistics();
      await loadModule();

      const html = callTooltip([[5], [3]], 0);
      expect(html).toContain('<div class="absence-statistics-tooltip-row absence-statistics-tooltip-row--total">');
      expect(html).toContain("8 Tage");
    });

    it("includes a color swatch per row using the series color", async function () {
      setAbsenceStatistics();
      await loadModule();

      const html = callTooltip([[5], [3]], 0);
      expect(html).toContain('style="background-color: yellow"');
      expect(html).toContain('style="background-color: violet"');
    });
  });

  describe("distribution chart", function () {
    it("builds one series value per type from the year sum, in model order", async function () {
      setAbsenceStatistics();
      await loadModule();

      expect(distributionChart().options.series).toEqual([8, 4]);
    });

    it("uses the model's labels and colors, in model order", async function () {
      setAbsenceStatistics();
      await loadModule();

      const { options } = distributionChart();
      expect(options.labels).toEqual(["Erholungsurlaub", "Sonderurlaub"]);
      expect(options.colors).toEqual(["var(--absence-color-YELLOW)", "var(--absence-color-VIOLET)"]);
    });

    it("hides the built-in ApexCharts legend - the server-rendered legend carries the numbers", async function () {
      setAbsenceStatistics();
      await loadModule();

      expect(distributionChart().options.legend.show).toBe(false);
    });

    it("renders into #distribution-chart and calls render", async function () {
      setAbsenceStatistics();
      await loadModule();

      expect(distributionChart().element).toBe(document.querySelector("#distribution-chart"));
      expect(distributionChart().render).toHaveBeenCalled();
    });

    it("shows name, days and share in its own tooltip", async function () {
      setAbsenceStatistics();
      await loadModule();

      const html = distributionChart().options.tooltip.custom({
        seriesIndex: 0,
        w: { config: { colors: ["yellow", "violet"] } },
      });
      expect(html).toContain('<div class="absence-statistics-tooltip-title">Erholungsurlaub</div>');
      expect(html).toContain("8 Tage");
      expect(html).toContain("66,7 %");
    });
  });

  describe("vacation ring", function () {
    it("creates exactly one series from the percentage in the model", async function () {
      setAbsenceStatistics();
      await loadModule();

      expect(ringChart().options.series).toEqual([67]);
    });

    it("starts at angle 0 and ends at 270", async function () {
      setAbsenceStatistics();
      await loadModule();

      const { radialBar } = ringChart().options.plotOptions;
      expect(radialBar.startAngle).toBe(0);
      expect(radialBar.endAngle).toBe(270);
    });

    it("formats the bar label as a percentage of the given series value", async function () {
      setAbsenceStatistics();
      await loadModule();

      const { formatter } = ringChart().options.plotOptions.radialBar.barLabels;
      expect(formatter("Stand heute", { seriesIndex: 0, w: { globals: { series: [67] } } })).toBe("67%");
    });

    it("disables the tooltip and legend", async function () {
      setAbsenceStatistics();
      await loadModule();

      const { options } = ringChart();
      expect(options.tooltip.enabled).toBe(false);
      expect(options.legend.show).toBe(false);
    });

    it("renders into #vacation-ring and calls render", async function () {
      setAbsenceStatistics();
      await loadModule();

      expect(ringChart().element).toBe(document.querySelector("#vacation-ring"));
      expect(ringChart().render).toHaveBeenCalled();
    });
  });

  describe("empty model", function () {
    it("builds empty series for the monthly and distribution charts without throwing", async function () {
      setAbsenceStatistics({ types: [] });

      await expect(loadModule()).resolves.toBeDefined();
      expect(monthlyChart().options.series).toEqual([]);
      expect(distributionChart().options.series).toEqual([]);
      expect(distributionChart().options.labels).toEqual([]);
    });

    it("still renders all three charts", async function () {
      setAbsenceStatistics({ types: [] });
      await loadModule();

      expect(monthlyChart().render).toHaveBeenCalled();
      expect(distributionChart().render).toHaveBeenCalled();
      expect(ringChart().render).toHaveBeenCalled();
    });
  });

  describe("theme handling", function () {
    it("uses dark theme mode for every chart when the current theme is dark", async function () {
      setAbsenceStatistics();
      themeObservable.value = "dark";
      await loadModule();

      for (const { options } of chartInstances) {
        expect(options.theme.mode).toBe("dark");
      }
    });

    it("uses light theme mode for every chart when the current theme is not dark", async function () {
      setAbsenceStatistics();
      themeObservable.value = "light";
      await loadModule();

      for (const { options } of chartInstances) {
        expect(options.theme.mode).toBe("light");
      }
    });

    it("updates every chart's theme mode when the theme changes afterwards", async function () {
      setAbsenceStatistics();
      await loadModule();

      themeObservable.value = "dark";
      await Promise.resolve();

      for (const chart of chartInstances) {
        expect(chart.updateOptions).toHaveBeenCalledWith({ theme: { mode: "dark" } });
      }
    });
  });

  describe("reduced motion handling", function () {
    it("disables animations for every chart when reduced motion is preferred", async function () {
      setAbsenceStatistics();
      reducedMotionObservable.value = true;
      await loadModule();

      for (const { options } of chartInstances) {
        expect(options.chart.animations.enabled).toBe(false);
      }
    });

    it("enables animations for every chart when reduced motion is not preferred", async function () {
      setAbsenceStatistics();
      reducedMotionObservable.value = false;
      await loadModule();

      for (const { options } of chartInstances) {
        expect(options.chart.animations.enabled).toBe(true);
      }
    });
  });

  it("reserves the chart, pie and ring sizes in the stylesheet", async function () {
    setAbsenceStatistics();
    await loadModule();

    const css = readFileSync("src/main/css/bundles/absence-statistics.css", "utf8");
    expect(css).toContain(`calc(${monthlyChart().options.chart.height}px +`);
    expect(css).toContain(`width: ${distributionChart().options.chart.width}px`);
    expect(css).toContain(`width: ${ringChart().options.chart.width}px`);
  });
});
