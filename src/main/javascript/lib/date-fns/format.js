import { format as dateFnsFormat } from "date-fns/format";
import resolveDateFnsLocale from "./locale-resolver";

export default function format(date, formatString, options = {}) {
  options.locale = resolveDateFnsLocale();
  return dateFnsFormat(date, formatString, options);
}
