import "../js/common";
import { useTheme } from "../js/use-theme";
import ApexCharts from "apexcharts/core";
import "apexcharts/radialBar";
import "apexcharts/bar";
import "apexcharts/line";
import "apexcharts/features/legend";
import "apexcharts/features/keyboard";
import { useMedia } from "../js/use-media";
import { apexOptionsWithPersistence } from "../js/charts/series-visibility-persistence";

// the chart hosts are empty until this bundle has executed, so css/bundles/sick-note-statistics.css
// reserves these sizes up front to keep the page from reflowing. keep both in sync.
const CHART_HEIGHT = 320;
const GAUGE_SIZE = 160;

// backend sends [currentYearSick, currentYearChildSick, previousYearSick, previousYearChildSick];
// entries of the same category (sick / child-sick) are at the same index modulo 2.
const dataseriesValues = globalThis.sicknoteStatistic.dataseriesValues;
const dataseriesNames = globalThis.sicknoteStatistic.dataseriesNames;
const xaxisLabels = globalThis.sicknoteStatistic.xaxisLabels;
const CATEGORY_COUNT = dataseriesNames.length;

// build one [previousYear, currentYear] pair per category (sick / child-sick).
const categoryPairs = dataseriesNames.map((name, categoryIndex) => {
  const pair = dataseriesValues.filter((_, index) => index % 2 === categoryIndex).toSorted((a, b) => a.year - b.year);
  return { name, previousYear: pair[0], currentYear: pair[1] };
});

// two grouped-stacked bars per month: previous year (sick + child-sick stacked, on the left)
// and current year (sick + child-sick stacked, on the right). ApexCharts groups bar series by
// their `group` value, positioning each unique group side by side in first-appearance order -
// so all "previousYear" series must come before the "currentYear" series in this array.
const barSeries = [
  ...categoryPairs.map(({ name, previousYear }) => ({
    name: `${name} ${previousYear.year}`,
    group: "previousYear",
    data: previousYear.data,
  })),
  ...categoryPairs.map(({ name, currentYear }) => ({
    name: `${name} ${currentYear.year}`,
    group: "currentYear",
    data: currentYear.data,
  })),
];

// the sick rate has its own chart below the days per month - it is a percentage rather than a day
// count, so it does not share an axis with the bars. backend sends [currentYear, previousYear].
const sickRateName = globalThis.sicknoteStatistic.sickRateName;
const [currentYearSickRate, previousYearSickRate] = globalThis.sicknoteStatistic.sickRateValues;
const RATE_CURRENT_INDEX = 0;
const RATE_PREVIOUS_INDEX = 1;
const rateSeries = [
  { name: `${sickRateName} ${currentYearSickRate.year}`, data: currentYearSickRate.data },
  { name: `${sickRateName} ${previousYearSickRate.year}`, data: previousYearSickRate.data },
];

function round1(number) {
  return Math.round(number * 10) / 10;
}

function formatTooltipValue(value, previousYearValue, unit = "") {
  if (previousYearValue === undefined) {
    return `${round1(value)}${unit}`;
  }

  const difference = round1(value - previousYearValue);
  const sign = difference > 0 ? "+" : "";
  if (previousYearValue === 0) {
    return `${round1(value)}${unit} (${sign}${difference}${unit})`;
  }

  const percentage = Math.round((difference / previousYearValue) * 100);
  const percentageSign = percentage > 0 ? "+" : "";
  return `${round1(value)}${unit} (${sign}${difference}${unit} / ${percentageSign}${percentage}%)`;
}

// one tooltip row per series, shared by both charts. previousValue undefined renders the bare
// value, otherwise the difference to the previous year is appended.
function buildTooltipRow({ series: seriesValues, dataPointIndex, w }, seriesIndex, previousValue, unit = "") {
  const value = seriesValues[seriesIndex][dataPointIndex];
  const name = w.globals.seriesNames[seriesIndex];
  const color = w.config.colors[seriesIndex];
  const valueText =
    previousValue === undefined ? `${round1(value)}${unit}` : formatTooltipValue(value, previousValue, unit);

  return `
    <div class="sicknote-statistics-tooltip-row">
      <span class="sicknote-statistics-tooltip-swatch" style="background-color: ${color}"></span>
      <span>${name}: ${valueText}</span>
    </div>
  `;
}

// the month is taken from the model rather than from w.globals.labels: apexcharts infers a numeric
// x-axis for the line chart, which leaves the month index there instead of the name.
function buildTooltip(dataPointIndex, rows) {
  const month = xaxisLabels[dataPointIndex];
  return `<div class="sicknote-statistics-tooltip-title">${month}</div>${rows.join("")}`;
}

// bar series are laid out as [previousYearCategory0, previousYearCategory1, ..., currentYearCategory0, ...];
// the current/previous counterpart of a series is offset by CATEGORY_COUNT.
function pairSeriesIndex(seriesIndex) {
  return seriesIndex < CATEGORY_COUNT ? seriesIndex + CATEGORY_COUNT : seriesIndex - CATEGORY_COUNT;
}

function isPreviousYearSeriesIndex(seriesIndex) {
  return seriesIndex < CATEGORY_COUNT;
}

const { theme } = useTheme();
const { matches: reducedMotion } = useMedia("(prefers-reduced-motion: reduce)");

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
  tooltip: {
    shared: true,
    intersect: false,
    // without this, ApexCharts anchors the tooltip on the hovered bar segment itself
    // (vertically centered on it), so it renders on top of the bar instead of beside it.
    followCursor: true,
    // custom row order (top to bottom): sick current, sick previous, child-sick current,
    // child-sick previous - independent of the series' internal render/group order.
    custom: function (tooltipContext) {
      const { series: seriesValues, dataPointIndex } = tooltipContext;

      const barRows = categoryPairs
        .flatMap((_, categoryIndex) => [CATEGORY_COUNT + categoryIndex, categoryIndex])
        .map((seriesIndex) => {
          const previousValue = isPreviousYearSeriesIndex(seriesIndex)
            ? undefined
            : seriesValues[pairSeriesIndex(seriesIndex)]?.[dataPointIndex];
          return buildTooltipRow(tooltipContext, seriesIndex, previousValue);
        });

      return buildTooltip(dataPointIndex, barRows);
    },
  },
  theme: {
    mode: theme.value === "dark" ? "dark" : "light",
  },
  // series order is [previousYearSick, previousYearChildSick, currentYearSick, currentYearChildSick]
  colors: [
    "var(--sick-note-color-light)",
    "var(--sick-note-child-color-light)",
    "var(--sick-note-color)",
    "var(--sick-note-child-color)",
  ],
  // transparent border creates a visible gap between adjacent bar segments (sick / child-sick,
  // previous / current year).
  stroke: {
    show: true,
    width: 2,
    colors: ["transparent"],
  },
  dataLabels: {
    enabled: true,
  },
  xaxis: {
    categories: xaxisLabels,
  },
  yaxis: {
    title: {
      text: globalThis.sicknoteStatistic.yaxisTitle,
    },
  },
  series: barSeries,
};

const chart = new ApexCharts(
  document.querySelector("#sicknote-statistic-chart"),
  apexOptionsWithPersistence(options, {
    key: "sicknote-statistics",

    // keep the local state clean:
    // - update version date on breaking changes to clean the persisted local state
    //   (order should be safe by deterministic key calculated with #getId )
    // raised when the sick rate moved into its own chart, a stored state would hide the wrong bar
    version: new Date("2026-08-05"),

    // map series element to id.
    // the id is used as key in persist layer to be independent of the array order.
    // so the order of the chart elements can be changed safely.
    getId({ name }) {
      const year = currentYearSickRate.year;
      const isComparison = !name.includes(year);

      const isSickDaysCountElement = dataseriesNames.some((label) => name.includes(label));
      if (isSickDaysCountElement) {
        if (name.includes(dataseriesNames[1])) {
          return isComparison ? "sick-count-child-compare" : "sick-count-child";
        } else {
          return isComparison ? "sick-count-compare" : "sick-count";
        }
      }

      return "";
    },
  }),
);

void chart.render();

// the sick rate of both years, one line each. same months as the bars above, but a percentage -
// which is why it is a chart of its own instead of a second axis on the bars.
const sickRateOptions = {
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
  // series order is [currentYearSickRate, previousYearSickRate]. a year keeps the colour it has in
  // the days chart above, so the same year reads as the same colour across both charts.
  colors: ["var(--sick-note-color)", "var(--sick-note-color-light)"],
  stroke: {
    show: true,
    width: 1,
    curve: "smooth",
  },
  // twelve months times two years would put 24 numbers into the plot - the exact values are
  // available in the tooltip instead
  dataLabels: {
    enabled: false,
  },
  xaxis: {
    categories: xaxisLabels,
  },
  yaxis: {
    title: {
      text: globalThis.sicknoteStatistic.sickRateYaxisTitle,
    },
    labels: {
      formatter: (value) => `${round1(value)}%`,
    },
  },
  tooltip: {
    shared: true,
    intersect: false,
    followCursor: true,
    custom: function (tooltipContext) {
      const { series: seriesValues, dataPointIndex } = tooltipContext;

      const rows = [
        buildTooltipRow(tooltipContext, RATE_CURRENT_INDEX, seriesValues[RATE_PREVIOUS_INDEX][dataPointIndex], "%"),
        buildTooltipRow(tooltipContext, RATE_PREVIOUS_INDEX, undefined, "%"),
      ];

      return buildTooltip(dataPointIndex, rows);
    },
  },
  series: rateSeries,
};

const sickRateChart = new ApexCharts(
  document.querySelector("#sicknote-statistic-sick-rate-chart"),
  apexOptionsWithPersistence(sickRateOptions, {
    key: "sicknote-statistics-sick-rate",

    version: new Date("2026-08-05"),

    // the id must not be the year: hiding the comparison line should keep it hidden after
    // switching the year
    getId({ name }) {
      return name.includes(currentYearSickRate.year) ? "sick-rate" : "sick-rate-compare";
    },
  }),
);

void sickRateChart.render();

const dataseriesValuesForAtLeastOneSickNotePercent = globalThis.sicknoteStatistic
  .dataseriesValuesForAtLeastOneSickNotePercent || [0, 0];

const atLeastOneSickNoteChart = new ApexCharts(document.querySelector("#sicknote-statistic-verteilung"), {
  chart: {
    type: "radialBar",
    height: GAUGE_SIZE,
    width: `${GAUGE_SIZE}px`,
    parentHeightOffset: 0,
    background: "var(--uv-chart-background)",
    toolbar: {
      show: false,
    },
    animations: {
      enabled: !reducedMotion.value,
      speed: 200,
    },
  },
  theme: {
    mode: theme.value === "dark" ? "dark" : "light",
  },
  series: dataseriesValuesForAtLeastOneSickNotePercent,
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
  plotOptions: {
    radialBar: {
      offsetY: 0,
      startAngle: 0,
      endAngle: 270,
      position: "front",
      hollow: {
        margin: 0,
        size: "30%",
        background: "var(--uv-chart-container-background)",
        position: "front",
      },
      track: {
        background: "var(--uv-chart-border)",
      },
      dataLabels: {
        name: {
          show: false,
        },
        value: {
          show: false,
        },
      },
      barLabels: {
        enabled: true,
        offsetX: -8,
        fontSize: "16px",
        formatter: function (seriesName, { seriesIndex, w }) {
          return w.globals.series[seriesIndex] + "%";
        },
      },
    },
  },
  // dataseriesValuesForAtLeastOneSickNotePercent is [currentYear, previousYear]
  colors: ["var(--sick-note-color)", "var(--sick-note-color-light)"],
  stroke: {
    lineCap: "round",
  },
  tooltip: {
    enabled: false,
  },
  legend: {
    show: false,
  },
});
atLeastOneSickNoteChart.render();

theme.subscribe(async function (nextTheme) {
  const mode = nextTheme === "dark" ? "dark" : "light";
  await Promise.all([
    chart.updateOptions({ theme: { mode } }),
    sickRateChart.updateOptions({ theme: { mode } }),
    atLeastOneSickNoteChart.updateOptions({ theme: { mode } }),
  ]);
});
