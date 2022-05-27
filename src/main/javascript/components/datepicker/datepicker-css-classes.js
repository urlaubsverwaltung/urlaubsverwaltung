import { isPublicHoliday, isPublicHolidayMorning, isPublicHolidayNoon } from "../../js/public-holiday";
import {
  isPersonalHolidayApprovedFull,
  isPersonalHolidayApprovedMorning,
  isPersonalHolidayApprovedNoon,
  isPersonalHolidayTemporaryFull,
  isPersonalHolidayTemporaryMorning,
  isPersonalHolidayTemporaryNoon,
  isPersonalHolidayWaitingFull,
  isPersonalHolidayWaitingMorning,
  isPersonalHolidayWaitingNoon,
  isSickNoteFull,
  isSickNoteMorning,
  isSickNoteNoon,
} from "../../js/absence";
import { isToday, isWeekend } from "date-fns";

const datepickerClassnames = {
  day: "datepicker-day",
  today: "datepicker-day-today",
  past: "datepicker-day-past",
  weekend: "datepicker-day-weekend",
  publicHolidayFull: "datepicker-day-public-holiday-full",
  publicHolidayMorning: "datepicker-day-public-holiday-morning",
  publicHolidayNoon: "datepicker-day-public-holiday-noon",
  absenceFull: "datepicker-day-absence-full",
  absenceFullApproved: "datepicker-day-absence-full-approved",
  absenceMorning: "datepicker-day-absence-morning",
  absenceMorningApproved: "datepicker-day-absence-morning-approved",
  absenceNoon: "datepicker-day-absence-noon",
  absenceNoonApproved: "datepicker-day-absence-noon-approved",
  sickNoteFull: "datepicker-day-sick-note-full",
  sickNoteMorning: "datepicker-day-sick-note-morning",
  sickNoteNoon: "datepicker-day-sick-note-noon",
};

const isPast = () => false;

/**
 *
 * @param node {HTMLElement}
 */
export function removeDatepickerCssClassesFromNode(node) {
  node.classList.remove(...Object.values(datepickerClassnames));
}

/**
 *
 * @param node {HTMLElement}
 * @param date {Date}
 * @param absences {Array}
 * @param publicHolidays {Array}
 */
export function addDatepickerCssClassesToNode(node, date, absences, publicHolidays) {
  const cssClasses = getCssClassesForDate(date, absences, publicHolidays);
  node.classList.add(...cssClasses);
}

function getCssClassesForDate(date, absences, publicHolidays) {
  return [
    datepickerClassnames.day,
    isToday(date) && datepickerClassnames.today,
    isPast() && datepickerClassnames.past,
    isWeekend(date) && datepickerClassnames.weekend,
    isPublicHoliday(publicHolidays) && datepickerClassnames.publicHolidayFull,
    isPublicHolidayMorning(publicHolidays) && datepickerClassnames.publicHolidayMorning,
    isPublicHolidayNoon(publicHolidays) && datepickerClassnames.publicHolidayNoon,
    isPersonalHolidayWaitingFull(absences) && datepickerClassnames.absenceFull,
    isPersonalHolidayTemporaryFull(absences) && datepickerClassnames.absenceFull,
    isPersonalHolidayApprovedFull(absences) && datepickerClassnames.absenceFullApproved,
    isPersonalHolidayWaitingMorning(absences) && datepickerClassnames.absenceMorning,
    isPersonalHolidayTemporaryMorning(absences) && datepickerClassnames.absenceMorning,
    isPersonalHolidayApprovedMorning(absences) && datepickerClassnames.absenceMorningApproved,
    isPersonalHolidayWaitingNoon(absences) && datepickerClassnames.absenceNoon,
    isPersonalHolidayTemporaryNoon(absences) && datepickerClassnames.absenceNoon,
    isPersonalHolidayApprovedNoon(absences) && datepickerClassnames.absenceNoonApproved,
    isSickNoteFull(absences) && datepickerClassnames.sickNoteFull,
    isSickNoteMorning(absences) && datepickerClassnames.sickNoteMorning,
    isSickNoteNoon(absences) && datepickerClassnames.sickNoteNoon,
  ].filter(Boolean);
}
