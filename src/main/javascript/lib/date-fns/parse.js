import { parse as dateFnsParse } from "date-fns/parse";
import resolveDateFnsLocale from "./locale-resolver";

export default function parse(dateString, formatString, referenceDate, options = {}) {
  options.locale = resolveDateFnsLocale();
  return dateFnsParse(dateString, formatString, referenceDate, options);
}
