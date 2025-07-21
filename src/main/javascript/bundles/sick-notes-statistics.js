import "../js/common";
import { useTheme } from "../js/use-theme";
import ApexCharts from "apexcharts";
import { useMedia } from "../js/use-media";

const series = globalThis.sicknoteStatistic.dataseriesNames.map((name, index) => ({
  name,
  data: globalThis.sicknoteStatistic.dataseriesValues[index].data,
}));

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
    zoom: {
      enabled: false,
    },
    selection: {
      enabled: false,
    },
  },
  legend: {
    position: "bottom",
  },
  legend: {
    position: "top",
    horizontalAlign: "right",
  },
  tooltip: {
    enabled: false,
  },
  theme: {
    // mode: theme.value === "dark" ? "dark" : "light",
    mode: theme.value === "dark" ? "dark" : "light",
  },
  responsive: [
    {
      breakpoint: 480,
      options: {},
    },
    {
      breakpoint: Number.MAX_SAFE_INTEGER,
      options: {},
    },
  ],
  colors: ["var(--sick-note-color)", "var(--sick-note-child-color)"],
  xaxis: {
    categories: globalThis.sicknoteStatistic.xaxisLabels,
  },
  yaxis: {
    title: {
      text: globalThis.sicknoteStatistic.yaxisTitle,
    },
  },
  series,
};

const chart = new ApexCharts(document.querySelector("#sicknote-statistic-chart"), options);
chart.render();

theme.subscribe(async function (nextTheme) {
  await chart.updateOptions({
    theme: {
      mode: nextTheme === "dark" ? "dark" : "light",
    },
  });
});
