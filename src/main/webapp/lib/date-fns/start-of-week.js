import startOfWeek from 'date-fns/start_of_week'

export default function startOfWeekI18nified(date, options = {}) {
  if (options.weekStartsOn == undefined) {
    options.weekStartsOn = window.uv.weekStartsOn;
  }

  return startOfWeek(date, options);
}
