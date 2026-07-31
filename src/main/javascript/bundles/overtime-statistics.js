import "../js/common";
import { useTheme } from "../js/use-theme";
import ApexCharts from "apexcharts/core";
import "apexcharts/bar";
import "apexcharts/features/legend";
import "apexcharts/features/keyboard";
import { useMedia } from "../js/use-media";

const statistics = globalThis.overtimeStatistics;

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
    height: 340,
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

const chart = new ApexCharts(document.querySelector("#overtime-statistics-chart"), options);
void chart.render();

theme.subscribe(async function (nextTheme) {
  const mode = nextTheme === "dark" ? "dark" : "light";
  await chart.updateOptions({ theme: { mode } });
});
