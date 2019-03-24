import en from 'date-fns/locale/en';

global.__datefnsLocale__ = global.__datefnsLocale__ || en;

export function setLocale(locale) {
  global.__datefnsLocale__ = locale;
}

export default function resolveLocale() {
  return global.__datefnsLocale__;
}
