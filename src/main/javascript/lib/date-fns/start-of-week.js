import { startOfWeek as dateFnsStartOfWeek } from "date-fns/startOfWeek";

export default function startOfWeek(date, options = {}) {
  if (options.weekStartsOn == undefined) {
    options.weekStartsOn = globalThis.uv.weekStartsOn;
  }

  return dateFnsStartOfWeek(date, options);
}
