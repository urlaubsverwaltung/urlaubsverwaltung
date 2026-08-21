import "../js/common";
import { useTheme } from "../js/use-theme";
import ApexCharts from "apexcharts/core";
import "apexcharts/radialBar";
import "apexcharts/bar";
import "apexcharts/pie";
import "apexcharts/features/legend";
import "apexcharts/features/keyboard";
import { useMedia } from "../js/use-media";
import { chartDefaults, noHoverStates } from "../js/charts/chart-defaults";
import { tooltipTitleHtml, tooltipRowHtml } from "../js/charts/chart-tooltip";

// Series-visibility persistence (apexOptionsWithPersistence) is intentionally not used here - it's
// optional per the task, only supports cartesian chart types (not the pie or the ring below), and
// this is a brand new page with no prior local state to preserve.

// the chart hosts are empty until this bundle has executed, so css/bundles/absence-statistics.css
// reserves these sizes up front to keep the page from reflowing. keep all three in sync.
const CHART_HEIGHT = 400;
const PIE_SIZE = 220;
const RING_SIZE = 180;

// backend sends types already sorted descending by year sum, so series,
// pie and legend all share the same order without this bundle re-deriving it.
const types = globalThis.absenceStatistics.types || [];
const xaxisLabels = globalThis.absenceStatistics.xaxisLabels;
const yaxisTitle = globalThis.absenceStatistics.yaxisTitle;
const vacationDaysTakenPercentage = globalThis.absenceStatistics.vacationDaysTakenPercentage ?? 0;

// document.documentElement.lang reflects the current request's locale (set via th:lang in
// _layout.html) - same source company-overview.js uses for its own Intl.NumberFormat calls, so
// the tooltip's number formatting matches the server-rendered legend for every locale, not just
// German.
const decimal = new Intl.NumberFormat(document.documentElement.lang, { maximumFractionDigits: 1 });

function colorOf(type) {
  return `var(--absence-color-${type.color})`;
}

function tooltipTitle(title, rows) {
  return tooltipTitleHtml("absence-statistics", title, rows);
}

function tooltipRow(color, text) {
  return tooltipRowHtml("absence-statistics", color, text);
}

const { theme } = useTheme();
const { matches: reducedMotion } = useMedia("(prefers-reduced-motion: reduce)");

function themeMode() {
  return theme.value === "dark" ? "dark" : "light";
}

/* ---------------------------------------------------------------- *
 * 1 - stacked bars, one segment per absence type, in model order
 * ---------------------------------------------------------------- */
const monthlyOptions = {
  chart: {
    type: "bar",
    stacked: true,
    height: CHART_HEIGHT,
    parentHeightOffset: 0,
    background: "var(--uv-chart-background)",
    ...chartDefaults(reducedMotion.value),
  },
  theme: {
    mode: themeMode(),
  },
  series: types.map((type) => ({ name: type.name, data: type.monthlyDays })),
  colors: types.map(colorOf),
  states: noHoverStates,
  legend: {
    position: "top",
    horizontalAlign: "left",
  },
  // eight stacked segments across twelve months would put ~96 numbers into the plot; the tooltip
  // carries the exact values instead.
  dataLabels: {
    enabled: false,
  },
  // a 2px gap in the surface colour keeps adjacent segments apart
  stroke: {
    show: true,
    width: 2,
    colors: ["transparent"],
  },
  xaxis: {
    categories: xaxisLabels,
  },
  yaxis: {
    title: { text: yaxisTitle },
  },
  tooltip: {
    shared: true,
    intersect: false,
    followCursor: true,
    custom: function ({ series, dataPointIndex, w }) {
      const rows = series
        .map((values, index) => ({
          name: w.globals.seriesNames[index],
          color: w.config.colors[index],
          value: values[dataPointIndex],
        }))
        // a type without a single day in this month doesn't get a tooltip row
        .filter((row) => row.value > 0)
        .toSorted((a, b) => b.value - a.value)
        .map((row) => tooltipRow(row.color, `${row.name}: ${decimal.format(row.value)} ${yaxisTitle}`));

      const monthTotal = series.reduce((sum, values) => sum + values[dataPointIndex], 0);
      const totalRow = `
        <div class="absence-statistics-tooltip-row absence-statistics-tooltip-row--total">
          <span>${decimal.format(monthTotal)} ${yaxisTitle}</span>
        </div>
      `;

      return `${tooltipTitle(xaxisLabels[dataPointIndex], rows)}${totalRow}`;
    },
  },
};

const monthlyChart = new ApexCharts(document.querySelector("#monthly-chart"), monthlyOptions);
void monthlyChart.render();

/* ---------------------------------------------------------------- *
 * 2 - distribution over the whole year; the numbers live in the
 *     server-rendered legend beside it, so the pie itself stays bare
 * ---------------------------------------------------------------- */
const distributionOptions = {
  chart: {
    type: "pie",
    height: PIE_SIZE,
    width: PIE_SIZE,
    parentHeightOffset: 0,
    background: "var(--uv-chart-background)",
    ...chartDefaults(reducedMotion.value),
  },
  theme: {
    mode: themeMode(),
  },
  series: types.map((type) => type.yearSum),
  labels: types.map((type) => type.name),
  colors: types.map(colorOf),
  states: noHoverStates,
  legend: {
    show: false,
  },
  dataLabels: {
    enabled: false,
  },
  stroke: {
    show: true,
    width: 2,
    colors: ["var(--uv-chart-container-background)"],
  },
  tooltip: {
    // same follow-cursor behaviour as the monthly chart's tooltip, for a consistent feel
    followCursor: true,
    custom: function ({ seriesIndex, w }) {
      const type = types[seriesIndex];
      return tooltipTitle(type.name, [
        tooltipRow(
          w.config.colors[seriesIndex],
          `${decimal.format(type.yearSum)} ${yaxisTitle} · ${decimal.format(type.share)} %`,
        ),
      ]);
    },
  },
};

const distributionChart = new ApexCharts(document.querySelector("#distribution-chart"), distributionOptions);
void distributionChart.render();

/* ---------------------------------------------------------------- *
 * 3 - vacation taken: a single ring. deliberately inverted from a
 *     "remaining" reading (100% - remaining%), so a year that's
 *     mostly used up ends near a full ring, not an empty one.
 * ---------------------------------------------------------------- */
const ringOptions = {
  chart: {
    type: "radialBar",
    height: RING_SIZE,
    width: RING_SIZE,
    parentHeightOffset: 0,
    background: "var(--uv-chart-background)",
    ...chartDefaults(reducedMotion.value),
  },
  theme: {
    mode: themeMode(),
  },
  series: [vacationDaysTakenPercentage],
  colors: ["var(--absence-vacation-ring-color)"],
  states: noHoverStates,
  stroke: {
    lineCap: "round",
  },
  legend: {
    show: false,
  },
  tooltip: {
    enabled: false,
  },
  plotOptions: {
    radialBar: {
      offsetY: 0,
      startAngle: 0,
      endAngle: 270,
      position: "front",
      hollow: {
        margin: 0,
        // roomy hollow center for the percentage label sitting inside it
        size: "62%",
        background: "var(--uv-chart-container-background)",
        position: "front",
      },
      track: {
        background: "var(--uv-chart-border)",
      },
      // centered in the hollow middle, not barLabels (which draws the value alongside the arc
      // itself - built for the sick note gauge's two concentric rings, where each ring needs its
      // own adjacent label; with only one ring here, that positioning cut across the arc instead
      // of sitting cleanly next to it).
      dataLabels: {
        name: { show: false },
        value: {
          show: true,
          fontSize: "22px",
          fontWeight: 600,
          offsetY: 8,
          // rounded to a whole number - the underlying percentage carries more precision (see
          // VacationDaysTaken) than a gauge label needs
          formatter: function (value) {
            return `${Math.round(value)}%`;
          },
        },
      },
    },
  },
};

const ringChart = new ApexCharts(document.querySelector("#vacation-ring"), ringOptions);
void ringChart.render();

theme.subscribe(async function (nextTheme) {
  const mode = nextTheme === "dark" ? "dark" : "light";
  await Promise.all([
    monthlyChart.updateOptions({ theme: { mode } }),
    distributionChart.updateOptions({ theme: { mode } }),
    ringChart.updateOptions({ theme: { mode } }),
  ]);
});
