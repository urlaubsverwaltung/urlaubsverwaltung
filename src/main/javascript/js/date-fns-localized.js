import { setLocale } from "../lib/date-fns/locale-resolver";

const language = globalThis.uv.language.slice(0, 2);
const subtag = globalThis.uv.language.slice(3);

switch (language) {
  case "de": {
    if (subtag.startsWith("AT")) {
      import("date-fns/locale/de-AT").then((module) => setLocale(module));
    } else {
      import("date-fns/locale/de").then((module) => setLocale(module));
    }
    break;
  }
  case "en": {
    if (subtag.startsWith("GB")) {
      import("date-fns/locale/en-GB").then((module) => setLocale(module));
    }
    break;
  }
  case "el": {
    import("date-fns/locale/el").then((module) => setLocale(module));
    break;
  }
  // No default
}
