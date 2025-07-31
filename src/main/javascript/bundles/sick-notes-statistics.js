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
  series: [(4 / 14) * 100],
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
      startAngle: -135,
      endAngle: 135,
      position: "front",
      hollow: {
        margin: 0,
        size: "54%",
        background: "#fff",
        position: "front",
        dropShadow: {
          enabled: true,
          top: 1,
          left: 0,
          blur: 1,
          opacity: 0.25,
        },
      },
      track: {
        background: "#bfe3fd",
      },
      dataLabels: {
        name: {
          show: false,
        },
        value: {
          show: true,
          fontSize: "20px",
          color: "#111",
          offsetY: 8,
          fontWeight: 600,
          formatter(value) {
            return Number.parseInt(value, 10) + "%";
          },
        },
      },
    },
  },
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
