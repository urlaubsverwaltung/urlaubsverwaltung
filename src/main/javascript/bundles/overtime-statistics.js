import "../js/common";
import { useTheme } from "../js/use-theme";
import ApexCharts from "apexcharts/core";
import "apexcharts/bar";
import "apexcharts/features/legend";
import "apexcharts/features/keyboard";
import { useMedia } from "../js/use-media";

const series = [
  {
    name: globalThis.overtimeStatistic.dataseriesName,
    data: globalThis.overtimeStatistic.dataseriesValues,
  },
];

const { theme } = useTheme();
const { matches: reducedMotion } = useMedia("(prefers-reduced-motion: reduce)");

const options = {
  chart: {
    type: "bar",
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
    show: false,
  },
  tooltip: {
    enabled: false,
  },
  theme: {
    mode: theme.value === "dark" ? "dark" : "light",
  },
  colors: ["var(--overtime-color)"],
  xaxis: {
    categories: globalThis.overtimeStatistic.xaxisLabels,
  },
  yaxis: {
    title: {
      text: globalThis.overtimeStatistic.yaxisTitle,
    },
  },
  series,
};

const chart = new ApexCharts(document.querySelector("#overtime-statistic-chart"), options);
chart.render();

theme.subscribe(async function (nextTheme) {
  const nextDark = nextTheme === "dark";
  await chart.updateOptions({
    theme: {
      mode: nextDark ? "dark" : "light",
    },
  });
});
