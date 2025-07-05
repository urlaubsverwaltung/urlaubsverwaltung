import "../js/common";
import ApexCharts from "apexcharts";

let series = globalThis.sicknoteStatistic.dataseriesNames.map((name, i) => ({
  name: name,
  data: globalThis.sicknoteStatistic.dataseriesValues[i].data,
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
  series: series,
};

const chart = new ApexCharts(document.querySelector("#sicknote-statistic-chart"), options);
chart.render();
