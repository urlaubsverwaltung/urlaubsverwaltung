// position-anchor not yet supported by firefox.
// https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/position-anchor
// this js purpose is only positioning if the popover.
// remove me as soon as position-anchor is supported.

// Difference between tooltip and popup / popover:
// see https://www.nngroup.com/articles/tooltip-guidelines/
//
// tldr;
// - Tooltips are mainly for Desktop, become visible on mouse hover or focus
// - Popovers are usually accompanied by an info icon and shown on click/touch

import { createPopper } from "@popperjs/core";

const cache = new WeakMap();

// with attaching listener to document with capture:true
// we loose the option to handle individual event.defaultPrevented.
// alternatives would be
// - a custom component
// - attaching listeners to every [popover]
//   - due to dynamically updated html -> use MutationObserver and attach listeners to every [popover]
// however, capturing the event seems to be the way for now.
document.addEventListener(
  "beforetoggle",
  function (event) {
    if (event.newState === "closed") {
      if (cache.has(event.target)) {
        cache.get(event.target).destroy();
        cache.delete(event.target);
      }
      return;
    }

    const { positionAnchor } = event.target.dataset;
    if (positionAnchor) {
      const anchorElement = document.querySelector("#" + positionAnchor);
      if (anchorElement) {
        const popperInstance = createPopper(anchorElement, event.target, {
          placement: "bottom-start",
          modifiers: [
            {
              name: "offset",
              options: {
                offset: [0, 4],
              },
            },
          ],
        });
        cache.set(event.target, popperInstance);
      }
    }
  },
  { capture: true },
);
