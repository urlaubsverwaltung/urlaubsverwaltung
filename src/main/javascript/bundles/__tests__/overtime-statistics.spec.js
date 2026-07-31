import { observable } from "../../js/observable";

vi.mock("../../js/common", () => ({}));
vi.mock("apexcharts/bar", () => ({}));
vi.mock("apexcharts/features/legend", () => ({}));
vi.mock("apexcharts/features/keyboard", () => ({}));

describe("overtime-statistics", function () {
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

    document.body.innerHTML = `<div id="overtime-statistics-chart"></div>`;
  });

  afterEach(function () {
    document.body.innerHTML = "";
    delete globalThis.overtimeStatistics;
  });

  function setOvertimeStatistics(overrides) {
    globalThis.overtimeStatistics = {
      xaxisLabels: ["Jan", "Feb", "Mär"],
      yaxisTitle: "Stunden",
      accruedName: "Aufbau",
      reductionName: "Abbau",
      balanceName: "Saldo",
      accrued: [2.5, 4, 0],
      reduction: [-2, -1.5, 0],
      accruedText: ["2 Std. 30 Min.", "4 Std.", "keine"],
      reductionText: ["2 Std.", "1 Std. 30 Min.", "keine"],
      balanceText: ["30 Min.", "2 Std. 30 Min.", "keine"],
      ...overrides,
    };
  }

  it("renders one stacked bar chart with accrual upwards and reduction downwards", async function () {
    setOvertimeStatistics();
    await import("../overtime-statistics.js");

    expect(chartInstances).toHaveLength(1);

    const chart = chartInstances[0];
    expect(chart.element).toBe(document.querySelector("#overtime-statistics-chart"));
    expect(chart.render).toHaveBeenCalled();
    expect(chart.options.chart.type).toBe("bar");
    expect(chart.options.chart.stacked).toBe(true);

    expect(chart.options.series).toEqual([
      { name: "Aufbau", data: [2.5, 4, 0] },
      { name: "Abbau", data: [-2, -1.5, 0] },
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
});
