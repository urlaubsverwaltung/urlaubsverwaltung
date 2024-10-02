/**
 * Update HtmlElements like adding or removing css classes because of enabled JavaScript.
 *
 * @param {HTMLElement|Document} rootElement root element to traverse. only descendants are updated, not the root element itself.
 */
export function updateHtmlElementAttributes(rootElement) {
  removeJsNoHidden(rootElement);
  updateCssClasses(rootElement);
}

function removeJsNoHidden(rootElement) {
  // show elements when JavaScript is available
  for (let element of rootElement.querySelectorAll("[data-js-no-hidden]")) {
    element.removeAttribute("hidden");
  }
}

function updateCssClasses(rootElement) {
  // add classes when JavaScript is available
  for (let element of rootElement.querySelectorAll("[data-js-class]")) {
    element.classList.add(...element.dataset.jsClass.split(" "));
  }

  // remove classes when JavaScript is available
  for (let element of rootElement.querySelectorAll("[data-js-class-remove]")) {
    const { jsClassRemove = "" } = element.dataset;
    for (let className of jsClassRemove.split(" ")) {
      element.classList.remove(className);
    }
  }
}
