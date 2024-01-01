const infoBanner = document.querySelector("#info-banner");

function getStyle() {
  return `:root { --info-banner-height: ${infoBanner?.getBoundingClientRect().height ?? 0}px; }`;
}

// injectStyle is globally defined in _layout
// eslint-disable-next-line no-undef
const style = injectStyle(getStyle());

// text could break after window resize. therefore recalculate new height.
window.addEventListener("resize", () => (style.textContent = getStyle()), {
  passive: true,
});
