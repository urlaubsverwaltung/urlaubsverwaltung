/** how long an element slides to its new place, e.g. the tooltip from one anchor to the next, in ms */
export const SLIDE_MS = 150;

/**
 * @returns {boolean} whether the user prefers reduced motion, read anew on every call
 */
export function prefersReducedMotion() {
  return globalThis.matchMedia?.("(prefers-reduced-motion: reduce)").matches ?? false;
}

/**
 * Slides the element from the offset to where it is now - after it has moved there already.
 *
 * @param {HTMLElement} element
 * @param {number} dx the horizontal offset of the previous place, in px
 * @param {number} [dy] the vertical offset of the previous place, in px
 * @returns {Animation|undefined} the slide, to cancel it; `undefined` when reduced motion is preferred
 */
export function slideFrom(element, dx, dy = 0) {
  if (prefersReducedMotion()) {
    return;
  }
  return element.animate([{ translate: `${dx}px ${dy}px` }, { translate: "0 0" }], {
    duration: SLIDE_MS,
    easing: "ease-out",
  });
}
