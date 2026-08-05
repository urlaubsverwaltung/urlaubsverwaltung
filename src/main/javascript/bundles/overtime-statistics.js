import "../js/common";
import { useTheme } from "../js/use-theme";
import ApexCharts from "apexcharts/core";
import "apexcharts/bar";
import "apexcharts/line";
import "apexcharts/features/legend";
import "apexcharts/features/keyboard";
import { useMedia } from "../js/use-media";
import { apexOptionsWithPersistence } from "../js/charts/series-visibility-persistence";

const statistics = globalThis.overtimeStatistics;

// the chart hosts are empty until this bundle has executed, so css/bundles/overtime-statistics.css
// reserves this height up front to keep the page from reflowing. keep both in sync.
const CHART_HEIGHT = 340;

const { theme } = useTheme();
const { matches: reducedMotion } = useMedia("(prefers-reduced-motion: reduce)");

// accrual and reduction are two series stacked around the zero line: the backend hands over the reduction negated, so
// accrual grows upwards and reduction downwards while both share one column per month.
const series = [
  { name: statistics.accruedName, data: statistics.accrued },
  { name: statistics.reductionName, data: statistics.reduction },
];

const colors = ["var(--overtime-accrued-color)", "var(--overtime-reduction-color)"];

function tooltipRow(color, label, value) {
  return `
    <div class="overtime-statistics-tooltip-row">
      <span class="overtime-statistics-tooltip-swatch" style="background-color: ${color}"></span>
      <span>${label}: ${value}</span>
    </div>
  `;
}

const options = {
  chart: {
    type: "bar",
    stacked: true,
    height: CHART_HEIGHT,
    parentHeightOffset: 0,
    background: "var(--uv-chart-background)",
    animations: {
      enabled: !reducedMotion.value,
      speed: 200,
    },
    toolbar: {
      show: false,
    },
  },
  states: {
    hover: {
      filter: {
        type: "none",
      },
    },
    active: {
      filter: {
        type: "none",
      },
    },
  },
  legend: {
    position: "top",
    horizontalAlign: "right",
  },
  theme: {
    mode: theme.value === "dark" ? "dark" : "light",
  },
  colors,
  series,
  plotOptions: {
    bar: {
      columnWidth: "55%",
      borderRadius: 4,
      borderRadiusApplication: "end",
    },
  },
  // a stroke in the surface colour keeps a visible gap where accrual and reduction meet at the zero line
  stroke: {
    show: true,
    width: 2,
    colors: ["var(--uv-chart-background)"],
  },
  // twelve months times two series would put 24 numbers into the plot, which is unreadable - the exact values are
  // available in the tooltip instead
  dataLabels: {
    enabled: false,
  },
  xaxis: {
    categories: statistics.xaxisLabels,
    axisBorder: {
      show: false,
    },
    axisTicks: {
      show: false,
    },
  },
  yaxis: {
    title: {
      text: statistics.yaxisTitle,
    },
  },
  tooltip: {
    shared: true,
    intersect: false,
    // without this, apexcharts anchors the tooltip on the hovered bar segment itself and renders on top of it
    followCursor: true,
    custom: function ({ dataPointIndex, w }) {
      const month = statistics.xaxisLabels[dataPointIndex];
      return `
        <div class="overtime-statistics-tooltip-title">${month}</div>
        ${tooltipRow(w.config.colors[0], statistics.accruedName, statistics.accruedText[dataPointIndex])}
        ${tooltipRow(w.config.colors[1], statistics.reductionName, statistics.reductionText[dataPointIndex])}
        ${tooltipRow("transparent", statistics.balanceName, statistics.balanceText[dataPointIndex])}
      `;
    },
  },
};

const chart = new ApexCharts(
  document.querySelector("#overtime-statistics-chart"),
  apexOptionsWithPersistence(options, {
    key: "overtime-statistics-chart",
    version: new Date("2026-08-01"),
    getId({ name }) {
      return name === statistics.accruedName ? "accrued" : "reduction";
    },
  }),
);
void chart.render();

// the balance curve: the balance added up month by month, one line per year so the years can be compared

const balanceColors = ["var(--overtime-balance-color)", "var(--overtime-balance-compare-color)"];

const balanceOptions = {
  chart: {
    type: "line",
    height: CHART_HEIGHT,
    parentHeightOffset: 0,
    background: "var(--uv-chart-background)",
    animations: {
      enabled: !reducedMotion.value,
      speed: 200,
    },
    toolbar: {
      show: false,
    },
  },
  states: {
    hover: {
      filter: {
        type: "none",
      },
    },
    active: {
      filter: {
        type: "none",
      },
    },
  },
  legend: {
    position: "top",
    horizontalAlign: "right",
  },
  theme: {
    mode: theme.value === "dark" ? "dark" : "light",
  },
  colors: balanceColors,
  series: statistics.balanceSeries.map((entry) => ({
    name: String(entry.year),
    data: entry.values,
  })),
  // the previous year is dashed as well, so the two lines can be told apart without relying on colour
  stroke: {
    width: 3,
    curve: "smooth",
    dashArray: [0, 4],
  },
  // no dots on the data points - the curve carries the trend, the exact values are in the tooltip.
  // apexcharts still shows a marker on hover.
  markers: {
    size: 0,
  },
  dataLabels: {
    enabled: false,
  },
  xaxis: {
    categories: statistics.xaxisLabels,
    axisBorder: {
      show: false,
    },
    axisTicks: {
      show: false,
    },
  },
  yaxis: {
    title: {
      text: statistics.balanceYaxisTitle,
    },
  },
  tooltip: {
    shared: true,
    intersect: false,
    followCursor: true,
    custom: function ({ dataPointIndex, w }) {
      const month = statistics.xaxisLabels[dataPointIndex];
      const rows = statistics.balanceSeries
        .map((entry, index) => tooltipRow(w.config.colors[index], String(entry.year), entry.valuesText[dataPointIndex]))
        .join("");
      return `<div class="overtime-statistics-tooltip-title">${month}</div>${rows}`;
    },
  },
};

const balanceChart = new ApexCharts(
  document.querySelector("#overtime-balance-chart"),
  apexOptionsWithPersistence(balanceOptions, {
    key: "overtime-statistics-balance",

    // keep the local state clean:
    // raise this date whenever the delivered series change, otherwise a stored state can hide the wrong line
    version: new Date("2026-08-01"),

    // the id must not be the year: hiding the comparison line should keep it hidden after switching the year
    getId({ name }) {
      return name === String(statistics.selectedYear) ? "balance" : "balance-compare";
    },
  }),
);
void balanceChart.render();

theme.subscribe(async function (nextTheme) {
  const mode = nextTheme === "dark" ? "dark" : "light";
  await Promise.all([chart.updateOptions({ theme: { mode } }), balanceChart.updateOptions({ theme: { mode } })]);
});
