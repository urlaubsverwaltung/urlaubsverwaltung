/**
 * Builds an ApexCharts custom tooltip's HTML: a title followed by one or more pre-built rows.
 *
 * Class names are prefixed with the given `classPrefix` (e.g. "absence-statistics") so each page
 * keeps styling this markup with its own CSS - only the JS shape (title + rows) is shared here.
 *
 * @param {string} classPrefix CSS class prefix, e.g. "absence-statistics"
 * @param {string} title tooltip title, usually the hovered x-axis label
 * @param {string[]} rows pre-built row HTML, e.g. from {@link tooltipRowHtml}
 * @return {string} the tooltip's HTML
 */
export function tooltipTitleHtml(classPrefix, title, rows) {
  return `<div class="${classPrefix}-tooltip-title">${title}</div>${rows.join("")}`;
}

/**
 * Builds one tooltip row: a colored swatch next to arbitrary content, usually "name: value".
 *
 * @param {string} classPrefix CSS class prefix, e.g. "absence-statistics"
 * @param {string} color the swatch's background color
 * @param {string} content row content
 * @return {string} the row's HTML
 */
export function tooltipRowHtml(classPrefix, color, content) {
  return `
    <div class="${classPrefix}-tooltip-row">
      <span class="${classPrefix}-tooltip-swatch" style="background-color: ${color}"></span>
      <span>${content}</span>
    </div>
  `;
}
