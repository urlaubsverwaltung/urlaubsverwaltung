import { observable } from "./observable";
import { useMedia } from "./use-media";

const html = document.querySelector("html");
const initialSystem = html.classList.contains("tw-system");
const initialDark = html.classList.contains("tw-dark");

const { matches: prefersDark } = useMedia("(prefers-color-scheme: dark)");

/**
 * @typedef {'light' | 'dark'} theme
 */

/**
 * Creates an observable theme container having value of type {@link theme}.
 *
 * @return {{theme: observable}}
 */
export function useTheme() {
  let theme;

  if (initialSystem) {
    theme = observable(prefersDark.value ? "dark" : "light");
    prefersDark.subscribe(function (nextMatch) {
      theme.value = nextMatch ? "dark" : "light";
    });
  } else {
    theme = observable(initialDark ? "dark" : "light");
  }

  return {
    theme,
  };
}
