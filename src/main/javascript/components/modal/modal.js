// modal (or dialog), meaning that while a popover is being shown,
// the rest of the page is rendered non-interactive until the popover is actioned in some way
// (for example an important choice is made).
//
// custom implementation of Invoker Commands Api (commandfor and command attributes)
// since not supported by safari yet (only in preview mode)
// https://developer.mozilla.org/en-US/docs/Web/API/Invoker_Commands_API
//

/**
 * @typedef Command
 * @type { "show-modal" | "close" }
 */

document.addEventListener("click", function (event) {
  if (event.defaultPrevented) {
    return;
  }

  /** @type HTMLElement */
  const target = event.target;

  const commandFor = target.getAttribute("commandfor");
  if (commandFor) {
    /** @type {Command} */
    const command = target.getAttribute("command");

    const targetElement = document.querySelector("#" + commandFor);
    if (targetElement) {
      if (command === "show-modal") {
        targetElement.showModal();
      } else if (command === "close") {
        targetElement.close();
      }
    }
  }
});
