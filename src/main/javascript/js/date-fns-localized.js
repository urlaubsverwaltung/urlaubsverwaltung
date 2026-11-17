import { setLocale } from "../lib/date-fns/locale-resolver";

const language = globalThis.uv.language.slice(0, 2);
const subtag = globalThis.uv.language.slice(3);

function loadLocale() {
  switch (language) {
    case "de": {
      if (subtag.startsWith("AT")) {
        return import("date-fns/locale/de-AT").then(({ deAT }) => setLocale(deAT));
      }
      return import("date-fns/locale/de").then(({ de }) => setLocale(de));
    }
    case "en": {
      if (subtag.startsWith("GB")) {
        return import("date-fns/locale/en-GB").then(({ enGB }) => setLocale(enGB));
      }
      return Promise.resolve();
    }
    case "el": {
      return import("date-fns/locale/el").then(({ el }) => setLocale(el));
    }
    default: {
      return Promise.resolve();
    }
  }
}

// the locale is loaded lazily, await this before formatting dates on page load or they are formatted in en-US
export const localeLoaded = loadLocale();
