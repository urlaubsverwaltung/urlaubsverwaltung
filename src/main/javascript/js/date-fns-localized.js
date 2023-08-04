import { setLocale } from "../lib/date-fns/locale-resolver";

const language = window.uv.language.slice(0, 2);
const subtag = window.uv.language.slice(3);

switch (language) {
  case "de": {
    if (subtag.startsWith("AT")) {
      import(/* webpackChunkName: "date-fn-locale-de-AT" */ "date-fns/locale/de-AT").then((module) =>
        setLocale(module.default),
      );
    } else {
      import(/* webpackChunkName: "date-fn-locale-de" */ "date-fns/locale/de").then((module) =>
        setLocale(module.default),
      );
    }

    break;
  }
  case "en": {
    if (subtag.startsWith("GB")) {
      import(/* webpackChunkName: "date-fn-locale-en-gb" */ "date-fns/locale/en-GB").then((module) =>
        setLocale(module.default),
      );
    }

    break;
  }
  case "el": {
    import(/* webpackChunkName: "date-fn-locale-el" */ "date-fns/locale/el").then((module) =>
      setLocale(module.default),
    );

    break;
  }
  // No default
}
