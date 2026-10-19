const ENTITIES = {
  "&": "&amp;",
  "<": "&lt;",
  ">": "&gt;",
  '"': "&quot;",
  "'": "&#39;",
};

/**
 * Escapes the given text, so it is rendered as text when inserted as html - e.g. into html strings handed to a
 * library which assigns them with innerHTML.
 *
 * @param {string} text
 * @return {string} the escaped text
 */
export function escapeHtml(text) {
  return String(text).replaceAll(/[&<>"']/g, (character) => ENTITIES[character]);
}

/**
 * Escapes the given text just enough to be rendered as text when inserted as element content: only "<" can start
 * markup there. Unlike {@link escapeHtml} it keeps "&" and quotes readable, for strings a library uses as plain text
 * as well - e.g. apexcharts inserts the legend formatter's result as html and also sets it as aria-label.
 *
 * Must not be used for attribute values.
 *
 * @param {string} text
 * @return {string} the escaped text
 */
export function escapeHtmlContent(text) {
  return String(text).replaceAll("<", "&lt;");
}
