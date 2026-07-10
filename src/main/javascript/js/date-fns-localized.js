import { setLocale } from "../lib/date-fns/locale-resolver";

const language = globalThis.uv.language.slice(0, 2);
const subtag = globalThis.uv.language.slice(3);

async function loadLocale() {
  // our webpack build cannot handle top-level-await, so this is wrapped in an async function
  switch (language) {
    case "de": {
      if (subtag.startsWith("AT")) {
        const { deAT } = await import("date-fns/locale/de-AT");
        setLocale(deAT);
      } else {
        const { de } = await import("date-fns/locale/de");
        setLocale(de);
      }
      break;
    }
    case "en": {
      if (subtag.startsWith("GB")) {
        const { enGB } = await import("date-fns/locale/en-GB");
        setLocale(enGB);
      }
      break;
    }
    case "el": {
      const { el } = await import("date-fns/locale/el");
      setLocale(el);
      break;
    }
    // No default
  }
}

loadLocale();
