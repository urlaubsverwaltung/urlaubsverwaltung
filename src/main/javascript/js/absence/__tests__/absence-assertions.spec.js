import * as assertions from "../absence-assertions";
import * as criteria from "../absence-criteria";

// pairs every exported predicate with the criteria object it is expected to match
const CASES = [
  ["isNoWorkday", "noWorkdayCriteria"],
  ["isSickNoteFull", "sickNoteFullCriteria"],
  ["isSickNoteWaitingFull", "sickNoteFullWaitingCriteria"],
  ["isSickNoteActiveFull", "sickNoteFullActiveCriteria"],
  ["isSickNoteMorning", "sickNoteMorningCriteria"],
  ["isSickNoteWaitingMorning", "sickNoteMorningWaitingCriteria"],
  ["isSickNoteActiveMorning", "sickNoteMorningActiveCriteria"],
  ["isSickNoteNoon", "sickNoteNoonCriteria"],
  ["isSickNoteWaitingNoon", "sickNoteNoonWaitingCriteria"],
  ["isSickNoteActiveNoon", "sickNoteNoonActiveCriteria"],
  ["isPersonalHolidayWaitingFull", "holidayFullWaitingCriteria"],
  ["isPersonalHolidayWaitingMorning", "holidayMorningWaitingCriteria"],
  ["isPersonalHolidayWaitingNoon", "holidayNoonWaitingCriteria"],
  ["isPersonalHolidayTemporaryFull", "holidayFullTemporaryCriteria"],
  ["isPersonalHolidayTemporaryMorning", "holidayMorningTemporaryCriteria"],
  ["isPersonalHolidayTemporaryNoon", "holidayNoonTemporaryCriteria"],
  ["isPersonalHolidayApprovedFull", "holidayFullApprovedCriteria"],
  ["isPersonalHolidayApprovedMorning", "holidayMorningApprovedCriteria"],
  ["isPersonalHolidayApprovedNoon", "holidayNoonApprovedCriteria"],
  ["isPersonalHolidayCancellationRequestedFull", "holidayFullCancellationRequestedCriteria"],
  ["isPersonalHolidayCancellationRequestedMorning", "holidayMorningCancellationRequestedCriteria"],
  ["isPersonalHolidayCancellationRequestedNoon", "holidayNoonCancellationRequestedCriteria"],
];

describe("absence-assertions", function () {
  describe.each(CASES)("%s", function (functionName, criteriaName) {
    const function_ = assertions[functionName];
    const matchingCriteria = criteria[criteriaName];

    it("returns true when an absence matches the criteria (plus unrelated extra fields)", function () {
      const absence = { ...matchingCriteria, someUnrelatedField: "whatever" };

      expect(function_([absence])).toBe(true);
    });

    it("returns true when the matching absence is not the first item", function () {
      const absence = { ...matchingCriteria };
      const unrelated = { absenceType: "SOME_OTHER_TYPE" };

      expect(function_([unrelated, unrelated, absence])).toBe(true);
    });

    it("returns false when no absence matches", function () {
      const absence = { ...matchingCriteria, absenceType: "SOME_OTHER_TYPE" };

      expect(function_([absence])).toBe(false);
    });

    it("returns false for an empty list", function () {
      expect(function_([])).toBe(false);
    });
  });

  describe("discriminates between morning/noon/full for the same status", function () {
    it("isPersonalHolidayApprovedFull does not match a morning or noon approved holiday", function () {
      expect(assertions.isPersonalHolidayApprovedFull([criteria.holidayMorningApprovedCriteria])).toBe(false);
      expect(assertions.isPersonalHolidayApprovedFull([criteria.holidayNoonApprovedCriteria])).toBe(false);
      expect(assertions.isPersonalHolidayApprovedFull([criteria.holidayFullApprovedCriteria])).toBe(true);
    });

    it("isSickNoteActiveMorning does not match an active noon or full sick note", function () {
      expect(assertions.isSickNoteActiveMorning([criteria.sickNoteNoonActiveCriteria])).toBe(false);
      expect(assertions.isSickNoteActiveMorning([criteria.sickNoteFullActiveCriteria])).toBe(false);
      expect(assertions.isSickNoteActiveMorning([criteria.sickNoteMorningActiveCriteria])).toBe(true);
    });
  });

  describe("discriminates between waiting/temporary/approved/cancellation-requested for the same period", function () {
    it("isPersonalHolidayWaitingFull does not match a temporary, approved or cancellation-requested full holiday", function () {
      expect(assertions.isPersonalHolidayWaitingFull([criteria.holidayFullTemporaryCriteria])).toBe(false);
      expect(assertions.isPersonalHolidayWaitingFull([criteria.holidayFullApprovedCriteria])).toBe(false);
      expect(assertions.isPersonalHolidayWaitingFull([criteria.holidayFullCancellationRequestedCriteria])).toBe(false);
      expect(assertions.isPersonalHolidayWaitingFull([criteria.holidayFullWaitingCriteria])).toBe(true);
    });
  });

  describe("isNoWorkday", function () {
    it("ignores absent/status fields entirely", function () {
      expect(assertions.isNoWorkday([{ absenceType: "NO_WORKDAY", absent: "FULL", status: "ANYTHING" }])).toBe(true);
    });

    it("does not match any other absenceType", function () {
      expect(assertions.isNoWorkday([{ absenceType: "VACATION" }])).toBe(false);
    });
  });
});
