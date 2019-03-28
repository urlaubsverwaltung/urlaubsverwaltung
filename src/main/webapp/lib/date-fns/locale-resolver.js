import en from 'date-fns/locale/en';

window.__datefnsLocale__ = window.__datefnsLocale__ || en;

export function setLocale(locale) {
  window.__datefnsLocale__ = locale;
}

export default function resolveLocale() {
  return window.__datefnsLocale__;
}
