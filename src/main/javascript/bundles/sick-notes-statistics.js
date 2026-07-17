import "../js/common";
import { useTheme } from "../js/use-theme";
import ApexCharts from "apexcharts/core";
import "apexcharts/radialBar";
import "apexcharts/bar";
import "apexcharts/line";
import "apexcharts/features/legend";
import "apexcharts/features/keyboard";
import { useMedia } from "../js/use-media";

// backend sends [currentYearSick, currentYearChildSick, previousYearSick, previousYearChildSick];
// entries of the same category (sick / child-sick) are at the same index modulo 2.
const dataseriesValues = globalThis.sicknoteStatistic.dataseriesValues;
const dataseriesNames = globalThis.sicknoteStatistic.dataseriesNames;
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

// sick rate is rendered as a line on a second (percentage) y-axis, overlaid on the bars above.
// backend sends [currentYear, previousYear].
const sickRateName = globalThis.sicknoteStatistic.sickRateName;
const [currentYearSickRate, previousYearSickRate] = globalThis.sicknoteStatistic.sickRateValues;
const RATE_CURRENT_INDEX = barSeries.length;
const RATE_PREVIOUS_INDEX = barSeries.length + 1;
const rateSeries = [
  { name: `${sickRateName} ${currentYearSickRate.year}`, type: "line", data: currentYearSickRate.data },
  { name: `${sickRateName} ${previousYearSickRate.year}`, type: "line", data: previousYearSickRate.data },
];

const series = [...barSeries, ...rateSeries];

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
    height: 320,
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
    // child-sick previous, sick rate current, sick rate previous - independent of the
    // series' internal render/group order.
    custom: function ({ series: seriesValues, dataPointIndex, w }) {
      const categoryLabel = w.globals.labels[dataPointIndex];

      const buildRow = (seriesIndex, previousValue, unit = "") => {
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
      };

      const barRows = categoryPairs
        .flatMap((_, categoryIndex) => [CATEGORY_COUNT + categoryIndex, categoryIndex])
        .map((seriesIndex) => {
          const previousValue = isPreviousYearSeriesIndex(seriesIndex)
            ? undefined
            : seriesValues[pairSeriesIndex(seriesIndex)]?.[dataPointIndex];
          return buildRow(seriesIndex, previousValue);
        });

      const rateRows = [
        buildRow(RATE_CURRENT_INDEX, seriesValues[RATE_PREVIOUS_INDEX][dataPointIndex], "%"),
        buildRow(RATE_PREVIOUS_INDEX, undefined, "%"),
      ];

      return `<div class="sicknote-statistics-tooltip-title">${categoryLabel}</div>${[...barRows, ...rateRows].join("")}`;
    },
  },
  theme: {
    mode: theme.value === "dark" ? "dark" : "light",
  },
  // series order is [previousYearSick, previousYearChildSick, currentYearSick, currentYearChildSick,
  // currentYearSickRate, previousYearSickRate]
  colors: [
    "var(--sick-note-color-light)",
    "var(--sick-note-child-color-light)",
    "var(--sick-note-color)",
    "var(--sick-note-child-color)",
    "var(--sick-rate-color)",
    "var(--sick-rate-color-light)",
  ],
  // transparent border creates a visible gap between adjacent bar segments (sick / child-sick,
  // previous / current year); the rate series get an actual stroke to render as a smooth line.
  stroke: {
    show: true,
    width: [2, 2, 2, 2, 3, 3],
    colors: [
      "transparent",
      "transparent",
      "transparent",
      "transparent",
      "var(--sick-rate-color)",
      "var(--sick-rate-color-light)",
    ],
    curve: "smooth",
  },
  markers: {
    size: [0, 0, 0, 0, 4, 4],
    strokeWidth: 0,
  },
  xaxis: {
    categories: globalThis.sicknoteStatistic.xaxisLabels,
  },
  yaxis: [
    {
      seriesName: barSeries.map((s) => s.name),
      title: {
        text: globalThis.sicknoteStatistic.yaxisTitle,
      },
    },
    {
      seriesName: rateSeries.map((s) => s.name),
      opposite: true,
      title: {
        text: globalThis.sicknoteStatistic.sickRateYaxisTitle,
      },
      labels: {
        formatter: (value) => `${round1(value)}%`,
      },
    },
  ],
  series,
};

const chart = new ApexCharts(document.querySelector("#sicknote-statistic-chart"), options);
chart.render();

const dataseriesValuesForAtLeastOneSickNotePercent = globalThis.sicknoteStatistic
  .dataseriesValuesForAtLeastOneSickNotePercent || [0, 0];

const atLeastOneSickNoteChart = new ApexCharts(document.querySelector("#sicknote-statistic-verteilung"), {
  chart: {
    type: "radialBar",
    height: 160,
    width: "160px",
    parentHeightOffset: 0,
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
    atLeastOneSickNoteChart.updateOptions({ theme: { mode } }),
  ]);
});
