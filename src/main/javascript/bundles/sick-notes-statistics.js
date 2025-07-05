import "../js/common";
import ApexCharts from "apexcharts";

const series = globalThis.sicknoteStatistic.dataseriesNames.map((name, index) => ({
  name,
  data: globalThis.sicknoteStatistic.dataseriesValues[index].data,
}));

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
    },
  },
  series,
};

const chart = new ApexCharts(document.querySelector("#sicknote-statistic-chart"), options);
chart.render();
