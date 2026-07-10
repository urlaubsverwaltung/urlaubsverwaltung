import { isPublicHoliday, isPublicHolidayMorning, isPublicHolidayNoon } from "../public-holiday-assertions";

describe("public-holiday-assertions", function () {
  describe("isPublicHoliday", function () {
    it("returns true when a FULL public holiday is present", function () {
      expect(isPublicHoliday([{ absencePeriodName: "FULL" }])).toBe(true);
    });

    it("returns false when only MORNING/NOON holidays are present", function () {
      expect(isPublicHoliday([{ absencePeriodName: "MORNING" }, { absencePeriodName: "NOON" }])).toBe(false);
    });

    it("returns false for an empty list", function () {
      expect(isPublicHoliday([])).toBe(false);
    });
  });

  describe("isPublicHolidayMorning", function () {
    it("returns true when a MORNING public holiday is present", function () {
      expect(isPublicHolidayMorning([{ absencePeriodName: "MORNING" }])).toBe(true);
    });

    it("returns false when no MORNING holiday is present", function () {
      expect(isPublicHolidayMorning([{ absencePeriodName: "FULL" }, { absencePeriodName: "NOON" }])).toBe(false);
    });
  });

  describe("isPublicHolidayNoon", function () {
    it("returns true when a NOON public holiday is present", function () {
      expect(isPublicHolidayNoon([{ absencePeriodName: "NOON" }])).toBe(true);
    });

    it("returns false when no NOON holiday is present", function () {
      expect(isPublicHolidayNoon([{ absencePeriodName: "FULL" }, { absencePeriodName: "MORNING" }])).toBe(false);
    });
  });
});
