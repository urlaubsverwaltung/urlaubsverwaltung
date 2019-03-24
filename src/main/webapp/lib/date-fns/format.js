import format from 'date-fns/format'
import resolveDateFnsLocale from './localeResolver'

export default function formatI18nified(date, formatStr, options = {}) {
  options.locale = resolveDateFnsLocale();
  return format(date, formatStr, options);
}
