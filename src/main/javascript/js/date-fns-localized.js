import { setLocale } from "../lib/date-fns/locale-resolver";

const language = globalThis.uv.language.slice(0, 2);
const subtag = globalThis.uv.language.slice(3);

switch (language) {
  case "de": {
    if (subtag.startsWith("AT")) {
      import("date-fns/locale/de-AT").then(({ deAT }) => setLocale(deAT));
    } else {
      import("date-fns/locale/de").then(({ de }) => setLocale(de));
    }
    break;
  }
  case "en": {
    if (subtag.startsWith("GB")) {
      import("date-fns/locale/en-GB").then(({ enGB }) => setLocale(enGB));
    }
    break;
  }
  case "el": {
    import("date-fns/locale/el").then(({ el }) => setLocale(el));
    break;
  }
  // No default
}
