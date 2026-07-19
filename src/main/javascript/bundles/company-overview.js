import "../js/common";
import { Idiomorph } from "idiomorph/dist/idiomorph.esm.js";

const frame = document.querySelector("#frame-company-overview");
const prefersReducedMotion = globalThis.matchMedia("(prefers-reduced-motion: reduce)").matches;

const LOADING_CARD_IDS = ["company-overview-card-overtime-average", "company-overview-card-overtime-distribution"];
let loadingTimer;

frame?.addEventListener("turbo:before-fetch-request", function () {
  clearTimeout(loadingTimer);
  loadingTimer = setTimeout(() => {
    for (const id of LOADING_CARD_IDS) {
      document.querySelector(`#${id}`)?.classList.add("statistic-card--is-loading");
    }
  }, 300);
});

frame?.addEventListener("turbo:before-fetch-response", clearLoadingIndicator);
frame?.addEventListener("turbo:fetch-request-error", clearLoadingIndicator);

function clearLoadingIndicator() {
  clearTimeout(loadingTimer);
  for (const id of LOADING_CARD_IDS) {
    document.querySelector(`#${id}`)?.classList.remove("statistic-card--is-loading");
  }
}

frame?.addEventListener("turbo:before-frame-render", (event) => {
  event.detail.render = function (currentElement, newElement) {
    const pendingHeights = [];
    const pendingNumbers = [];

    Idiomorph.morph(currentElement, newElement, {
      callbacks: {
        beforeNodeMorphed(oldNode, newNode) {
          if (oldNode.matches?.(".overtime-chart-column__bar")) {
            const oldPersonCountNode = oldNode.querySelector(".overtime-chart-column__person-count");
            const newPersonCountNode = newNode.querySelector(".overtime-chart-column__person-count");
            oldPersonCountNode.setAttribute("aria-label", newPersonCountNode.getAttribute("aria-label"));

            pendingHeights.push([oldNode, newNode.style.height]);
            pendingNumbers.push([oldPersonCountNode, oldPersonCountNode.textContent, newPersonCountNode.textContent]);

            return false;
          }
          if (oldNode.matches?.(".company-overtime-average-value, .company-overtime-growth-value")) {
            pendingNumbers.push([oldNode, oldNode.textContent, newNode.textContent]);
            return false;
          }
        },
      },
    });

    for (const [barElement, newHeight] of pendingHeights) {
      requestAnimationFrame(() => {
        barElement.style.height = newHeight;
      });
    }

    for (const [element, oldValue, newValue] of pendingNumbers) {
      const signed = element.matches(".company-overtime-growth-value");
      animateNumber(element, oldValue, newValue, signed);
    }
  };
});

/**
 *
 * @param {HTMLElement} element
 * @param {string} oldValue
 * @param {string} toValue
 * @param {boolean} [signed]
 * @param {number} [duration]
 */
function animateNumber(element, oldValue, toValue, signed = false, duration = 300) {
  const from = parseLocaleNumber(oldValue);
  const to = parseLocaleNumber(toValue);

  if (prefersReducedMotion || from.value === to.value) {
    element.textContent = toValue;
    return;
  }

  const start = performance.now();

  function step(now) {
    const progress = Math.min((now - start) / duration, 1);
    const value = from.value + (to.value - from.value) * easeInOutQuad(progress);
    element.textContent = new Intl.NumberFormat(document.documentElement.lang, {
      minimumFractionDigits: to.decimals,
      maximumFractionDigits: to.decimals,
      signDisplay: signed ? "exceptZero" : "auto",
    }).format(value);

    if (progress < 1) {
      requestAnimationFrame(step);
    }
  }

  requestAnimationFrame(step);
}

function parseLocaleNumber(text) {
  const decimals = (text.split(/[.,]/)[1] ?? "").length;
  return { value: Number.parseFloat(text.replace(",", ".")), decimals };
}

function easeInOutQuad(t) {
  return t < 0.5 ? 2 * t * t : 1 - (-2 * t + 2) ** 2 / 2;
}
