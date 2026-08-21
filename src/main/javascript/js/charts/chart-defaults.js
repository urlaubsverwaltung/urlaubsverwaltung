/**
 * The `chart.animations`/`chart.toolbar` options shared by every ApexCharts instance on the
 * statistics pages: animations follow the user's reduced-motion preference, and the built-in
 * toolbar (zoom/pan/download icons) is always hidden - none of these charts are interactive in
 * that way.
 *
 * @param {boolean} reducedMotion whether the user prefers reduced motion
 * @return {{ animations: ApexChart.ApexOptions["chart"]["animations"], toolbar: ApexChart.ApexOptions["chart"]["toolbar"] }}
 *   options to spread into an ApexCharts `chart` option object
 */
export function chartDefaults(reducedMotion) {
  return {
    animations: {
      enabled: !reducedMotion,
      speed: 200,
    },
    toolbar: {
      show: false,
    },
  };
}

/**
 * Disables ApexCharts' default hover/active highlight (e.g. dimming other series on hover) - every
 * chart on the statistics pages relies on its own custom tooltip instead.
 *
 * @type {ApexChart.ApexOptions["states"]}
 */
export const noHoverStates = {
  hover: { filter: { type: "none" } },
  active: { filter: { type: "none" } },
};
