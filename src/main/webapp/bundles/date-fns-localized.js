import { setLocale } from '../lib/date-fns/locale-resolver'

if (window.navigator.language.slice(0, 2) === 'de') {
  import(/* webpackChunkName: "date-fn-locale-de" */'date-fns/locale/de').then(setLocale);
}

