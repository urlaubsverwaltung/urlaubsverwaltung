import { readFileSync } from "node:fs";
import { observable } from "../../js/observable";

vi.mock("../../js/common", () => ({}));
vi.mock("apexcharts/bar", () => ({}));
vi.mock("apexcharts/line", () => ({}));
vi.mock("apexcharts/features/legend", () => ({}));
vi.mock("apexcharts/features/keyboard", () => ({}));

describe("overtime-statistics", function () {
  let chartInstances;
  let MockApexCharts;
  let themeObservable;
  let reducedMotionObservable;

  beforeEach(function () {
    vi.resetModules();
    localStorage.clear();
    globalThis.uv = { userId: "user-1" };

    chartInstances = [];
    MockApexCharts = class {
      constructor(element, options) {
        this.element = element;
        this.options = options;
        this.render = vi.fn();
        this.updateOptions = vi.fn().mockResolvedValue();
        chartInstances.push(this);
      }
    };

    themeObservable = observable("light");
    reducedMotionObservable = observable(false);

    vi.doMock("apexcharts/core", () => ({ default: MockApexCharts }));
    vi.doMock("../../js/use-theme", () => ({ useTheme: () => ({ theme: themeObservable }) }));
    vi.doMock("../../js/use-media", () => ({ useMedia: () => ({ matches: reducedMotionObservable }) }));

    document.body.innerHTML = `
      <div id="overtime-statistics-chart"></div>
      <div id="overtime-balance-chart"></div>
    `;
  });

  afterEach(function () {
    document.body.replaceChildren();
    delete globalThis.overtimeStatistics;
    delete globalThis.uv;
    localStorage.clear();
  });

  function setOvertimeStatistics(overrides) {
    globalThis.overtimeStatistics = {
      xaxisLabels: ["Jan", "Feb", "Mär"],
      tooltipLabels: ["Januar", "Februar", "März"],
      yaxisTitle: "Stunden",
      accruedName: "Aufbau",
      reductionName: "Abbau",
      balanceName: "Saldo",
      // the backend sends the selected year first
      monthlySeries: [
        {
          year: 2026,
          accrued: [2.5, 4, 0],
          reduction: [-2, -1.5, 0],
          accruedText: ["2 Std. 30 Min.", "4 Std.", "keine"],
          reductionText: ["2 Std.", "1 Std. 30 Min.", "keine"],
          balanceText: ["30 Min.", "2 Std. 30 Min.", "keine"],
        },
        {
          year: 2025,
          accrued: [1, 2, 3],
          reduction: [-0.5, -1, -2],
          accruedText: ["1 Std.", "2 Std.", "3 Std."],
          reductionText: ["30 Min.", "1 Std.", "2 Std."],
          balanceText: ["30 Min.", "1 Std.", "1 Std."],
        },
      ],
      selectedYear: 2026,
      balanceYaxisTitle: "Stunden (kumuliert)",
      balanceSeries: [
        { year: 2026, values: [0.5, 3, 3], valuesText: ["30 Min.", "3 Std.", "3 Std."] },
        { year: 2025, values: [1, 1.5, 2], valuesText: ["1 Std.", "1 Std. 30 Min.", "2 Std."] },
      ],
      ...overrides,
    };
  }

  it("renders one stacked bar chart with accrual upwards and reduction downwards", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    expect(chartInstances).toHaveLength(2);

    const chart = chartInstances[0];
    expect(chart.element).toBe(document.querySelector("#overtime-statistics-chart"));
    expect(chart.render).toHaveBeenCalled();
    expect(chart.options.chart.type).toBe("bar");
    expect(chart.options.chart.stacked).toBe(true);

    // the previous year first, so apexcharts puts its group on the left.
    // hidden is added by the visibility persistence the chart is wrapped in
    expect(chart.options.series).toEqual([
      { name: "Aufbau 2025", group: "2025", data: [1, 2, 3], hidden: false },
      { name: "Abbau 2025", group: "2025", data: [-0.5, -1, -2], hidden: false },
      { name: "Aufbau 2026", group: "2026", data: [2.5, 4, 0], hidden: false },
      { name: "Abbau 2026", group: "2026", data: [-2, -1.5, 0], hidden: false },
    ]);
  });

  it("draws only the selected year when the previous one has nothing to show", async function () {
    setOvertimeStatistics({
      monthlySeries: [
        {
          year: 2026,
          accrued: [2.5, 4, 0],
          reduction: [-2, -1.5, 0],
          accruedText: ["2 Std. 30 Min.", "4 Std.", "keine"],
          reductionText: ["2 Std.", "1 Std. 30 Min.", "keine"],
          balanceText: ["30 Min.", "2 Std. 30 Min.", "keine"],
        },
      ],
    });
    await import("../overtime-statistics.js");

    expect(chartInstances[0].options.series).toHaveLength(2);
  });

  it("leaves the bars square, apexcharts would round the selected year only", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    expect(chartInstances[0].options.plotOptions.bar.borderRadius).toBeUndefined();
  });

  it("shows the previous year in the lighter shade of the same colour", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    expect(chartInstances[0].options.colors).toEqual([
      "var(--overtime-accrued-color-light)",
      "var(--overtime-reduction-color-light)",
      "var(--overtime-accrued-color)",
      "var(--overtime-reduction-color)",
    ]);
  });

  it("uses the months as x-axis categories", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    expect(chartInstances[0].options.xaxis.categories).toEqual(["Jan", "Feb", "Mär"]);
    expect(chartInstances[0].options.yaxis.title.text).toBe("Stunden");
  });

  it("does not label every single bar", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    expect(chartInstances[0].options.dataLabels.enabled).toBe(false);
  });

  it("keeps a legend so the two series are not told apart by colour alone", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    expect(chartInstances[0].options.legend.show).not.toBe(false);
  });

  it("shows accrual, reduction and balance of the hovered month in the tooltip", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    const { tooltip, colors } = chartInstances[0].options;
    const html = tooltip.custom({ dataPointIndex: 1, w: { config: { colors } } });

    expect(html).toContain("Feb");
    expect(html).toContain("Aufbau");
    expect(html).toContain("4 Std.");
    expect(html).toContain("Abbau");
    expect(html).toContain("1 Std. 30 Min.");
    expect(html).toContain("Saldo");
    expect(html).toContain("2 Std. 30 Min.");
    // every label must come from the backend, a missing one would silently render as "undefined"
    expect(html).not.toContain("undefined");
  });

  it("blocks the tooltip by figure, the selected year first", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    const { tooltip, colors } = chartInstances[0].options;
    const html = tooltip.custom({ dataPointIndex: 1, w: { config: { colors } } });

    const order = [
      "Aufbau 2026: 4 Std.",
      "Aufbau 2025: 2 Std.",
      "Abbau 2026",
      "Abbau 2025",
      "Saldo 2026",
      "Saldo 2025",
    ];
    const positions = order.map((label) => html.indexOf(label));

    expect(positions).not.toContain(-1);
    expect(positions).toEqual(positions.toSorted((a, b) => a - b));
  });

  it("does not show a negative sign for the reduction in the tooltip", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    const { tooltip, colors } = chartInstances[0].options;
    const html = tooltip.custom({ dataPointIndex: 0, w: { config: { colors } } });

    expect(html).not.toContain("-2 Std.");
  });

  it("follows the theme when it changes", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    expect(chartInstances[0].options.theme.mode).toBe("light");

    themeObservable.value = "dark";
    await Promise.resolve();

    expect(chartInstances[0].updateOptions).toHaveBeenCalledWith({ theme: { mode: "dark" } });
  });

  it("disables animations when the user prefers reduced motion", async function () {
    reducedMotionObservable = observable(true);
    vi.doMock("../../js/use-media", () => ({ useMedia: () => ({ matches: reducedMotionObservable }) }));

    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    expect(chartInstances[0].options.chart.animations.enabled).toBe(false);
  });

  describe("balance chart", function () {
    it("renders a line per year on its own element", async function () {
      setOvertimeStatistics();
      await import("../overtime-statistics.js");

      const chart = chartInstances[1];
      expect(chart.element).toBe(document.querySelector("#overtime-balance-chart"));
      expect(chart.render).toHaveBeenCalled();
      expect(chart.options.chart.type).toBe("line");

      expect(chart.options.series).toEqual([
        expect.objectContaining({ name: "2026", data: [0.5, 3, 3] }),
        expect.objectContaining({ name: "2025", data: [1, 1.5, 2] }),
      ]);
    });

    it("draws only the selected year when the previous one has nothing to show", async function () {
      setOvertimeStatistics({
        balanceSeries: [{ year: 2026, values: [0.5, 3, 3], valuesText: ["30 Min.", "3 Std.", "3 Std."] }],
      });
      await import("../overtime-statistics.js");

      expect(chartInstances[1].options.series).toHaveLength(1);
    });

    it("names every year with its value in the tooltip", async function () {
      setOvertimeStatistics();
      await import("../overtime-statistics.js");

      const { tooltip, colors } = chartInstances[1].options;
      const html = tooltip.custom({ dataPointIndex: 1, w: { config: { colors } } });

      // the axis is abbreviated, the tooltip spells the month out
      expect(html).toContain("Februar");
      expect(html).toContain("2026");
      expect(html).toContain("3 Std.");
      expect(html).toContain("2025");
      expect(html).toContain("1 Std. 30 Min.");
      expect(html).not.toContain("undefined");
    });

    it("does not label every single point", async function () {
      setOvertimeStatistics();
      await import("../overtime-statistics.js");

      expect(chartInstances[1].options.dataLabels.enabled).toBe(false);
    });

    it("restores a series the user hid before the reload", async function () {
      localStorage.setItem(
        "uv:chart-series-visibility:user-1:overtime-statistics-balance",
        JSON.stringify({ version: new Date("2026-08-01").toISOString(), hiddenIds: ["balance-compare"] }),
      );

      setOvertimeStatistics();
      await import("../overtime-statistics.js");

      const series = chartInstances[1].options.series;
      expect(series[0].hidden).toBe(false);
      expect(series[1].hidden).toBe(true);
    });

    it("ignores a persisted state that belongs to an older data structure", async function () {
      localStorage.setItem(
        "uv:chart-series-visibility:user-1:overtime-statistics-balance",
        JSON.stringify({ version: new Date("2020-01-01").toISOString(), hiddenIds: ["balance-compare"] }),
      );

      setOvertimeStatistics();
      await import("../overtime-statistics.js");

      expect(chartInstances[1].options.series[1].hidden).toBe(false);
    });
  });

  // the chart hosts are empty until this bundle has executed - the css has to reserve the box up
  // front, otherwise the page paints collapsed charts and reflows by their full height afterwards.
  it("reserves every chart height in the stylesheet", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    const css = readFileSync("src/main/css/bundles/overtime-statistics.css", "utf8");

    for (const { options } of chartInstances) {
      expect(css).toContain(`calc(${options.chart.height}px +`);
    }
  });
});
