import { findWhere } from "underscore";
import {
  holidayFullApprovedCriteria,
  holidayFullCancellationRequestedCriteria,
  holidayFullTemporaryCriteria,
  holidayFullWaitingCriteria,
  holidayMorningApprovedCriteria,
  holidayMorningCancellationRequestedCriteria,
  holidayMorningTemporaryCriteria,
  holidayMorningWaitingCriteria,
  holidayNoonApprovedCriteria,
  holidayNoonCancellationRequestedCriteria,
  holidayNoonTemporaryCriteria,
  holidayNoonWaitingCriteria,
  noWorkdayCriteria,
  sickNoteFullActiveCriteria,
  sickNoteFullCriteria,
  sickNoteFullWaitingCriteria,
  sickNoteMorningActiveCriteria,
  sickNoteMorningCriteria,
  sickNoteMorningWaitingCriteria,
  sickNoteNoonActiveCriteria,
  sickNoteNoonCriteria,
  sickNoteNoonWaitingCriteria,
} from "./absence-criteria";

export function isNoWorkday(absences) {
  return Boolean(findWhere(absences, noWorkdayCriteria));
}

export function isSickNoteFull(absences) {
  return Boolean(findWhere(absences, sickNoteFullCriteria));
}

export function isSickNoteWaitingFull(absences) {
  return Boolean(findWhere(absences, sickNoteFullWaitingCriteria));
}

export function isSickNoteActiveFull(absences) {
  return Boolean(findWhere(absences, sickNoteFullActiveCriteria));
}

export function isSickNoteMorning(absences) {
  return Boolean(findWhere(absences, sickNoteMorningCriteria));
}

export function isSickNoteWaitingMorning(absences) {
  return Boolean(findWhere(absences, sickNoteMorningWaitingCriteria));
}

export function isSickNoteActiveMorning(absences) {
  return Boolean(findWhere(absences, sickNoteMorningActiveCriteria));
}

export function isSickNoteNoon(absences) {
  return Boolean(findWhere(absences, sickNoteNoonCriteria));
}

export function isSickNoteWaitingNoon(absences) {
  return Boolean(findWhere(absences, sickNoteNoonWaitingCriteria));
}

export function isSickNoteActiveNoon(absences) {
  return Boolean(findWhere(absences, sickNoteNoonActiveCriteria));
}

export function isPersonalHolidayWaitingFull(absences) {
  return Boolean(findWhere(absences, holidayFullWaitingCriteria));
}

export function isPersonalHolidayWaitingMorning(absences) {
  return Boolean(findWhere(absences, holidayMorningWaitingCriteria));
}

export function isPersonalHolidayWaitingNoon(absences) {
  return Boolean(findWhere(absences, holidayNoonWaitingCriteria));
}

export function isPersonalHolidayTemporaryFull(absences) {
  return Boolean(findWhere(absences, holidayFullTemporaryCriteria));
}

export function isPersonalHolidayTemporaryMorning(absences) {
  return Boolean(findWhere(absences, holidayMorningTemporaryCriteria));
}

export function isPersonalHolidayTemporaryNoon(absences) {
  return Boolean(findWhere(absences, holidayNoonTemporaryCriteria));
}

export function isPersonalHolidayApprovedFull(absences) {
  return Boolean(findWhere(absences, holidayFullApprovedCriteria));
}

export function isPersonalHolidayApprovedMorning(absences) {
  return Boolean(findWhere(absences, holidayMorningApprovedCriteria));
}

export function isPersonalHolidayApprovedNoon(absences) {
  return Boolean(findWhere(absences, holidayNoonApprovedCriteria));
}

export function isPersonalHolidayCancellationRequestedFull(absences) {
  return Boolean(findWhere(absences, holidayFullCancellationRequestedCriteria));
}

export function isPersonalHolidayCancellationRequestedMorning(absences) {
  return Boolean(findWhere(absences, holidayMorningCancellationRequestedCriteria));
}

export function isPersonalHolidayCancellationRequestedNoon(absences) {
  return Boolean(findWhere(absences, holidayNoonCancellationRequestedCriteria));
}
