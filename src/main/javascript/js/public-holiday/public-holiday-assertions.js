import { findWhere } from "underscore";

export function isPublicHoliday(publicHolidays) {
  return Boolean(findWhere(publicHolidays, { absencePeriodName: "FULL" }));
}

export function isPublicHolidayMorning(publicHolidays) {
  return Boolean(findWhere(publicHolidays, { absencePeriodName: "MORNING" }));
}

export function isPublicHolidayNoon(publicHolidays) {
  return Boolean(findWhere(publicHolidays, { absencePeriodName: "NOON" }));
}
