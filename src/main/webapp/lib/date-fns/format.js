import format from 'date-fns/format'
import resolveDateFnsLocale from './locale-resolver'

export default function formatI18nified(date, formatString, options = {}) {
  options.locale = resolveDateFnsLocale();
  return format(date, formatString, options);
}
