import { setLocale } from "../lib/date-fns/locale-resolver";

if (window.navigator.language.slice(0, 2) === "de") {
  if (window.navigator.language.slice(3, 5) === "AT") {
    import(/* webpackChunkName: "date-fn-locale-de-AT" */ "date-fns/locale/de-AT").then((module) =>
      setLocale(module.default),
    );
  } else {
    import(/* webpackChunkName: "date-fn-locale-de" */ "date-fns/locale/de").then((module) =>
      setLocale(module.default),
    );
  }
}
