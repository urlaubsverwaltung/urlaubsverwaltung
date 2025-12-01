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

/**
 * @typedef Placement
 * @type {'auto'
 *   | 'auto-start'
 *   | 'auto-end'
 *   | 'top'
 *   | 'top-start'
 *   | 'top-end'
 *   | 'bottom'
 *   | 'bottom-start'
 *   | 'bottom-end'
 *   | 'right'
 *   | 'right-start'
 *   | 'right-end'
 *   | 'left'
 *   | 'left-start'
 *   | 'left-end'}
 * @default 'auto'
 * @description replace with css https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/position-anchor when supported
 */

/**
 * @typedef AnchorSize
 * @type {'width'|'height'}
 * @description replace with css https://developer.mozilla.org/de/docs/Web/CSS/Reference/Values/anchor-size when supported
 */

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
    /** @type HTMLElement */
    const popover = event.target;

    if (event.newState === "closed") {
      if (cache.has(popover)) {
        cache.get(popover).destroy();
        cache.delete(popover);
      }
      return;
    }

    /** id of the anchor element */
    const anchor = popover.dataset.anchor;
    /** @type Placement */
    const placement = popover.dataset.anchorPlacement ?? "auto";
    /** @type AnchorSize */
    const anchorSize = popover.dataset.anchorSize;

    if (anchor) {
      const anchorElement = document.querySelector("#" + anchor);
      if (anchorElement) {
        // listen to anchor resizing and adapt popover dimensions
        const resizeObserver = new ResizeObserver((entries) => {
          for (let entry of entries) {
            if (anchorSize === "width") {
              popover.style.width = entry.borderBoxSize[0].inlineSize + "px";
            } else if (anchorSize === "height") {
              popover.style.height = entry.borderBoxSize[0].blockSize + "px";
            }
          }
        });
        if (anchorSize) {
          // start listening to resize event.
          // this also sets the initial width.
          resizeObserver.observe(anchorElement);
        }

        const popperInstance = createPopper(anchorElement, popover, {
          placement,
          modifiers: [
            {
              name: "offset",
              options: {
                offset: [0, 4],
              },
            },
          ],
        });

        cache.set(popover, {
          destroy() {
            popperInstance.destroy();
            resizeObserver.disconnect();
          },
        });
      }
    }
  },
  { capture: true },
);
