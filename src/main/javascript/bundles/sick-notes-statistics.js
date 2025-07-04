import "../js/common";
import ApexCharts from "apexcharts";

const options = {
  chart: {
    type: "bar",
    stacked: true,
  },
  theme: {
    mode: "light",
  },
  colors: ["var(--sick-note-color)", "var(--sick-note-child-color)"],
  xaxis: {
    categories: globalThis.sicknoteStatistic.xaxisLabels,
  },
  yaxis: {
    title: {
      text: globalThis.sicknoteStatistic.yaxisTitle,
      rotate: 0,
    },
  },
  series: globalThis.sicknoteStatistic.dataseries,
};

const chart = new ApexCharts(document.querySelector("#sicknote-statistic-chart"), options);
chart.render();
