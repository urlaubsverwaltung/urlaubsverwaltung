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
    stacked: false,
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
    enabled: false,
  },
  theme: {
    mode: theme.value === "dark" ? "dark" : "light",
  },
  colors: ["var(--sick-note-color)", "var(--sick-note-child-color)", "var(--sick-note-color-light)", "var(--sick-note-child-color-light)"],
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

const dataseriesValuesForAtLeastOneSickNotePercent = globalThis.sicknoteStatistic.dataseriesValuesForAtLeastOneSickNotePercent || [0, 0];

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
        background: "#fff",
        position: "front",
      },
      track: {
        background: "#bfe3fd",
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
        fontSize: '16px',
        formatter: function(seriesName, opts) {
          return opts.w.globals.series[opts.seriesIndex] + "%";
        },
      },
    },
  },
  colors: ["#1e9dfc", "#9fbed6"],
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
  const nextDark = nextTheme === "dark";
  await chart.updateOptions({
    theme: {
      mode: nextDark ? "dark" : "light",
    },
  });
});
