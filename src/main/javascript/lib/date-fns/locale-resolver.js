import { enUS as en } from "date-fns/locale/en-US";

globalThis.__datefnsLocale__ = globalThis.__datefnsLocale__ || en;

export function setLocale(locale) {
  globalThis.__datefnsLocale__ = locale;
}

export default function resolveLocale() {
  return globalThis.__datefnsLocale__;
}
