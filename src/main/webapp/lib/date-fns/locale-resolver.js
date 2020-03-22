// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import en from 'date-fns/locale/en-US';

window.__datefnsLocale__ = window.__datefnsLocale__ || en;
window.__datefnsLocaleChangedListener__ = window.__datefnsLocaleChangedListener__ || [];

export function setLocale(locale) {
  window.__datefnsLocale__ = locale;
}

export function signalLocaleChange() {
  window.__datefnsLocaleChangedListener__.forEach(listener => listener());
}

export function subscribeToLocaleChanged(listener) {
  window.__datefnsLocaleChangedListener__.push(listener);
}

export default function resolveLocale() {
  return window.__datefnsLocale__;
}
