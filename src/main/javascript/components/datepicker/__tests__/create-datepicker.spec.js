import { createDatepicker } from "../create-datepicker";
import DatepickerEN from "../locale/en";
import DatepickerDE from "../locale/de";
import fetchMock from "fetch-mock";
import de from "date-fns/locale/de";
import { setLocale } from "../../../lib/date-fns/locale-resolver";

describe("create-datepicker", () => {
  beforeEach(() => {
    window.uv = {
      datepicker: {
        localisation: datepickerLocalisation(),
      },
    };
    setLocale(de);
  });

  afterEach(async () => {
    // cleanup DOM
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }

    // and reset mocks
    fetchMock.restore();
  });

  test("replaces elements matching the selector with the @duetds/date-picker", async () => {
    document.body.innerHTML = `
      <input id="awesome-date-picker" />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;

    const result = await createDatepicker("#awesome-date-picker", { urlPrefix, getPersonId });

    const duetDatePicker = document.querySelector("#awesome-date-picker").closest("duet-date-picker");
    expect(duetDatePicker).toBeTruthy();
    expect(result).toBe(duetDatePicker);
  });

  test("sets 'duet-radius' to 0", async () => {
    document.body.innerHTML = `
      <input />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;

    const datepicker = await createDatepicker("input", { urlPrefix, getPersonId });

    expect(datepicker.getAttribute("style")).toBe("--duet-radius=0");
  });

  test("assigns original 'id' attribute", async () => {
    document.body.innerHTML = `
      <input id="awesome-date" />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;

    const datepicker = await createDatepicker("input", { urlPrefix, getPersonId });

    expect(datepicker.querySelector("input").getAttribute("id")).toBe("awesome-date");
  });

  test("assigns original 'class' attribute", async () => {
    document.body.innerHTML = `
      <input class="foo bar" />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;

    const datepicker = await createDatepicker("input", { urlPrefix, getPersonId });

    expect(datepicker.classList).toContain("foo");
    expect(datepicker.classList).toContain("bar");
  });

  test("assigns original 'name' attribute to the visible input element", async () => {
    document.body.innerHTML = `
      <input name="start-date" />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;

    const datepicker = await createDatepicker("input", { urlPrefix, getPersonId });

    const actualElement = datepicker.querySelector('input[name="start-date"]');
    expect(actualElement.getAttribute("type")).not.toBe("hidden");
    expect(actualElement.getAttribute("placeholder")).toBe("placeholder-message");
  });

  test("fails to render with preset date value but missing iso-value", async () => {
    expect.hasAssertions();

    document.body.innerHTML = `
      <input value="24.12.2020" />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;

    try {
      await createDatepicker("input", { urlPrefix, getPersonId });
    } catch (error) {
      expect(error.message).toEqual("date input defines a value but no `data-iso-value` attribute is given.");
    }
  });

  test("assigns original 'value' attribute", async () => {
    document.body.innerHTML = `
      <input value="24.12.2020" data-iso-value="2020-12-24" />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;

    const datepicker = await createDatepicker("input", { urlPrefix, getPersonId });

    expect(datepicker.getAttribute("value")).toBe("2020-12-24");
  });

  test('invokes "onSelect" when date value has been changed', async () => {
    document.body.innerHTML = `
      <input value="24.12.2020" data-iso-value="2020-12-24" />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;
    const onSelect = jest.fn();

    const datepicker = await createDatepicker("input", { urlPrefix, getPersonId, onSelect });

    expect(onSelect).not.toHaveBeenCalled();

    const event = new CustomEvent("duetChange");
    fireEvent(datepicker, event);

    expect(onSelect).toHaveBeenCalledWith(event);
  });

  test("initialises duet-date-picker with EN adapter for uv localisation 'en' config", async () => {
    window.uv.datepicker.localisation.locale = "en";

    document.body.innerHTML = `
      <input id="awesome-date-picker" />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;

    await createDatepicker("#awesome-date-picker", { urlPrefix, getPersonId });

    const duetDatePicker = document.querySelector("duet-date-picker");
    expect(duetDatePicker).toBeTruthy();
    expect(duetDatePicker.dateAdapter).toBe(DatepickerEN.dateAdapter);
  });

  test("initialises duet-date-picker with DE adapter for uv localisation 'de' config", async () => {
    window.uv.datepicker.localisation.locale = "de";

    document.body.innerHTML = `
      <input id="awesome-date-picker" />
    `;

    const urlPrefix = "";
    const getPersonId = () => 42;

    await createDatepicker("#awesome-date-picker", { urlPrefix, getPersonId });

    const duetDatePicker = document.querySelector("duet-date-picker");
    expect(duetDatePicker).toBeTruthy();
    expect(duetDatePicker.dateAdapter).toBe(DatepickerDE.dateAdapter);
  });

  describe.each([["en"], ["de"], ["de-de"]])("with browser locale '%s'", (givenLanguage) => {
    beforeEach(() => {
      jest.spyOn(window.navigator, "language", "get").mockReturnValue(givenLanguage);
    });

    describe("fetches absences and public-holidays when datepicker is opened", () => {
      beforeEach(async () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-12-01&to=2020-12-31`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-12-01&to=2020-12-31`, {
          absences: [],
        });

        document.body.innerHTML = `
          <input value="24.12.2020" data-iso-value="2020-12-24" />
        `;

        const urlPrefix = "my-url-prefix";
        const getPersonId = () => 42;

        await createDatepicker("input", { urlPrefix, getPersonId });

        // duet datepicker must to be opened to update month and year select-box values
        document.querySelector("button.duet-date__toggle").click();
        fetchMock.resetHistory();
      });

      test("with toggle button click", async () => {
        const button = document.querySelector("button.duet-date__toggle");

        expect(fetchMock.calls()).toHaveLength(0);

        button.click();

        expect(fetchMock.calls()).toHaveLength(2);
      });

      test("after month has been changed", async () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-01-01&to=2020-01-31`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-01-01&to=2020-01-31`, {
          absences: [],
        });

        const monthElement = document.querySelector(".duet-date__select--month");

        expect(fetchMock.calls()).toHaveLength(0);

        monthElement.value = "0";
        fireEvent.change(monthElement);

        expect(fetchMock.calls()).toHaveLength(2);
      });

      test("after year has been changed", async () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2019-12-01&to=2019-12-31`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2019-12-01&to=2019-12-31`, {
          absences: [],
        });

        const yearElement = document.querySelector(".duet-date__select--year");

        expect(fetchMock.calls()).toHaveLength(0);

        yearElement.value = "2019";
        fireEvent.change(yearElement);

        expect(fetchMock.calls()).toHaveLength(2);
      });

      test("after next button has been clicked", async () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-11-01&to=2020-11-30`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-11-01&to=2020-11-30`, {
          absences: [],
        });

        const previousMonthButton = document.querySelector(".duet-date__prev");

        expect(fetchMock.calls()).toHaveLength(0);

        previousMonthButton.click();
        expect(fetchMock.calls()).toHaveLength(2);
      });

      test("after prev button has been clicked", async () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2021-01-01&to=2021-01-31`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2021-01-01&to=2021-01-31`, {
          absences: [],
        });

        const nextMonthButton = document.querySelector(".duet-date__next");

        expect(fetchMock.calls()).toHaveLength(0);

        nextMonthButton.click();
        expect(fetchMock.calls()).toHaveLength(2);
      });
    });

    describe("clears public holiday markers", function () {
      beforeEach(async () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-12-01&to=2020-12-31`, {
          publicHolidays: [
            {
              date: "2020-12-25",
              absencePeriodName: "FULL",
            },
            {
              date: "2020-12-26",
              absencePeriodName: "FULL",
            },
          ],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-12-01&to=2020-12-31`, {
          absences: [],
        });

        document.body.innerHTML = `
          <input value="24.12.2020" data-iso-value="2020-12-24" />
        `;

        const urlPrefix = "my-url-prefix";
        const getPersonId = () => 42;

        await createDatepicker("input", { urlPrefix, getPersonId });

        // duet datepicker must to be opened to update month and year select-box values
        document.querySelector("button.duet-date__toggle").click();
        await new Promise((resolve) => setTimeout(resolve, 2));
        fetchMock.resetHistory();
      });

      test("after month has been changed", () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-01-01&to=2020-01-31`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-01-01&to=2020-01-31`, {
          absences: [],
        });

        const monthElement = document.querySelector(".duet-date__select--month");

        expect(document.querySelector(".datepicker-public-holiday-marker")).not.toBeNull();

        monthElement.value = "0";
        fireEvent.change(monthElement);

        expect(document.querySelector(".datepicker-public-holiday-marker")).toBeNull();
      });

      test("after year has been changed", () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2019-12-01&to=2019-12-31`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2019-12-01&to=2019-12-31`, {
          absences: [],
        });

        const yearElement = document.querySelector(".duet-date__select--year");

        expect(document.querySelector(".datepicker-public-holiday-marker")).not.toBeNull();

        yearElement.value = "2019";
        fireEvent.change(yearElement);

        expect(document.querySelector(".datepicker-public-holiday-marker")).toBeNull();
      });

      test("after next button has been clicked", () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-11-01&to=2020-11-30`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-11-01&to=2020-11-30`, {
          absences: [],
        });

        const previousMonthButton = document.querySelector(".duet-date__prev");

        expect(document.querySelector(".datepicker-public-holiday-marker")).not.toBeNull();

        previousMonthButton.click();
        expect(document.querySelector(".datepicker-public-holiday-marker")).toBeNull();
      });

      test("after prev button has been clicked", () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2021-01-01&to=2021-01-31`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2021-01-01&to=2021-01-31`, {
          absences: [],
        });

        const nextMonthButton = document.querySelector(".duet-date__next");

        expect(document.querySelector(".datepicker-public-holiday-marker")).not.toBeNull();

        nextMonthButton.click();
        expect(document.querySelector(".datepicker-public-holiday-marker")).toBeNull();
      });
    });

    describe("highlights days", () => {
      test("weekend", async () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-12-01&to=2020-12-31`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-12-01&to=2020-12-31`, {
          absences: [],
        });

        document.body.innerHTML = `
          <input value="24.12.2020" data-iso-value="2020-12-24" />
        `;

        const urlPrefix = "my-url-prefix";
        const getPersonId = () => 42;

        await createDatepicker("input", { urlPrefix, getPersonId });

        // fetch public holidays and update view
        document.querySelector("button.duet-date__toggle").click();

        // wait for response and css class calculation
        await new Promise((resolve) => setTimeout(resolve, 0));

        const getElement = (dateString) => {
          for (let span of document.querySelectorAll(".duet-date__vhidden")) {
            if (span.textContent === dateString) {
              return span.parentNode;
            }
          }
          throw new Error("could not find date element for dateString=" + dateString);
        };

        const assertWeekend = (dateString) => {
          const element = getElement(dateString);
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).toContain("datepicker-day-weekend");
        };

        assertWeekend("5. Dezember");
        assertWeekend("6. Dezember");
        assertWeekend("12. Dezember");
        assertWeekend("13. Dezember");
        assertWeekend("19. Dezember");
        assertWeekend("20. Dezember");
        assertWeekend("26. Dezember");
        assertWeekend("27. Dezember");
      });

      test("no public holidays", async () => {
        fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-12-01&to=2020-12-31`, {
          publicHolidays: [],
        });

        fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-12-01&to=2020-12-31`, {
          absences: [],
        });

        document.body.innerHTML = `
          <input value="24.12.2020" data-iso-value="2020-12-24" />
        `;

        const urlPrefix = "my-url-prefix";
        const getPersonId = () => 42;

        await createDatepicker("input", { urlPrefix, getPersonId });

        // fetch public holidays and update view
        document.querySelector("button.duet-date__toggle").click();

        // wait for response and css class calculation
        await new Promise((resolve) => setTimeout(resolve, 0));

        expect([...document.querySelectorAll("span.datepicker-public-holiday-marker")]).toHaveLength(0);
      });

      describe("public holiday", () => {
        function mockPublicHolidays(publicHolidays) {
          fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-12-01&to=2020-12-31`, {
            publicHolidays,
          });
        }

        beforeEach(async () => {
          fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-12-01&to=2020-12-31`, {
            absences: [],
          });

          document.body.innerHTML = `
            <input value="24.12.2020" data-iso-value="2020-12-24" />
          `;

          const urlPrefix = "my-url-prefix";
          const getPersonId = () => 42;

          await createDatepicker("input", { urlPrefix, getPersonId });
        });

        test("full", async () => {
          mockPublicHolidays([
            {
              date: "2020-12-24",
              absencePeriodName: "FULL",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.querySelector("span.datepicker-public-holiday-marker")).toBeDefined();
        });

        test("morning", async () => {
          mockPublicHolidays([
            {
              date: "2020-12-24",
              absencePeriodName: "MORNING",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.querySelector("span.datepicker-public-holiday-marker")).toBeDefined();
        });

        test("noon", async () => {
          mockPublicHolidays([
            {
              date: "2020-12-24",
              absencePeriodName: "NOON",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.querySelector("span.datepicker-public-holiday-marker")).toBeDefined();
        });
      });

      describe("vacation waiting", () => {
        function mockVacationWaitingAbsences(absences) {
          fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-12-01&to=2020-12-31`, {
            absences,
          });
        }

        beforeEach(async () => {
          fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-12-01&to=2020-12-31`, {
            publicHolidays: [],
          });

          document.body.innerHTML = `
            <input value="24.12.2020" data-iso-value="2020-12-24" />
          `;

          const urlPrefix = "my-url-prefix";
          const getPersonId = () => 42;

          await createDatepicker("input", { urlPrefix, getPersonId });
        });

        test("full", async () => {
          mockVacationWaitingAbsences([
            {
              date: "2020-12-24",
              absencePeriodName: "FULL",
              type: "VACATION",
              status: "WAITING",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).toContain("datepicker-day-personal-holiday-full");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-morning");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-noon");
        });

        test("morning", async () => {
          mockVacationWaitingAbsences([
            {
              date: "2020-12-24",
              absencePeriodName: "MORNING",
              type: "VACATION",
              status: "WAITING",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-full");
          expect(element.classList).toContain("datepicker-day-personal-holiday-morning");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-noon");
        });

        test("noon", async () => {
          mockVacationWaitingAbsences([
            {
              date: "2020-12-24",
              absencePeriodName: "NOON",
              type: "VACATION",
              status: "WAITING",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-full");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-morning");
          expect(element.classList).toContain("datepicker-day-personal-holiday-noon");
        });
      });

      describe("vacation approved", () => {
        function mockVacationApprovedAbsences(absences) {
          fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-12-01&to=2020-12-31`, {
            absences,
          });
        }

        beforeEach(async () => {
          fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-12-01&to=2020-12-31`, {
            publicHolidays: [],
          });

          document.body.innerHTML = `
            <input value="24.12.2020" data-iso-value="2020-12-24" />
          `;

          const urlPrefix = "my-url-prefix";
          const getPersonId = () => 42;

          await createDatepicker("input", { urlPrefix, getPersonId });
        });

        test("full", async () => {
          mockVacationApprovedAbsences([
            {
              date: "2020-12-24",
              absencePeriodName: "FULL",
              type: "VACATION",
              status: "ALLOWED",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).toContain("datepicker-day-personal-holiday-full-approved");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-morning-approved");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-noon-approved");
        });

        test("morning", async () => {
          mockVacationApprovedAbsences([
            {
              date: "2020-12-24",
              absencePeriodName: "MORNING",
              type: "VACATION",
              status: "ALLOWED",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-full-approved");
          expect(element.classList).toContain("datepicker-day-personal-holiday-morning-approved");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-noon-approved");
        });

        test("noon", async () => {
          mockVacationApprovedAbsences([
            {
              date: "2020-12-24",
              absencePeriodName: "NOON",
              type: "VACATION",
              status: "ALLOWED",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-full-approved");
          expect(element.classList).not.toContain("datepicker-day-personal-holiday-morning-approved");
          expect(element.classList).toContain("datepicker-day-personal-holiday-noon-approved");
        });
      });

      describe("sick day", () => {
        function mockSickDayAbsences(absences) {
          fetchMock.mock(`my-url-prefix/persons/42/absences?from=2020-12-01&to=2020-12-31`, {
            absences,
          });
        }

        beforeEach(async () => {
          fetchMock.mock(`my-url-prefix/persons/42/public-holidays?from=2020-12-01&to=2020-12-31`, {
            publicHolidays: [],
          });

          document.body.innerHTML = `
            <input value="24.12.2020" data-iso-value="2020-12-24" />
          `;

          const urlPrefix = "my-url-prefix";
          const getPersonId = () => 42;

          await createDatepicker("input", { urlPrefix, getPersonId });
        });

        test("full", async () => {
          mockSickDayAbsences([
            {
              date: "2020-12-24",
              absencePeriodName: "FULL",
              type: "SICK_NOTE",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).toContain("datepicker-day-sick-note-full");
          expect(element.classList).not.toContain("datepicker-day-sick-note-morning");
          expect(element.classList).not.toContain("datepicker-day-sick-note-noon");
        });

        test("morning", async () => {
          mockSickDayAbsences([
            {
              date: "2020-12-24",
              absencePeriodName: "MORNING",
              type: "SICK_NOTE",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).not.toContain("datepicker-day-sick-note-full");
          expect(element.classList).toContain("datepicker-day-sick-note-morning");
          expect(element.classList).not.toContain("datepicker-day-sick-note-noon");
        });

        test("noon", async () => {
          mockSickDayAbsences([
            {
              date: "2020-12-24",
              absencePeriodName: "NOON",
              type: "SICK_NOTE",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24. Dezember");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).not.toContain("datepicker-day-sick-note-full");
          expect(element.classList).not.toContain("datepicker-day-sick-note-morning");
          expect(element.classList).toContain("datepicker-day-sick-note-noon");
        });
      });
    });
  });

  test.each([["en"], ["de"]])("formats date with 'dd.MM.yyyy' for browser locale=%s", async (givenLanguage) => {
    jest.spyOn(window.navigator, "language", "get").mockReturnValue(givenLanguage);

    document.body.innerHTML = `
      <input value="24.12.2020" data-iso-value="2020-12-24" />
    `;

    const urlPrefix = "my-url-prefix";
    const getPersonId = () => 42;

    await createDatepicker("input", { urlPrefix, getPersonId });

    expect(document.querySelector("input").value).toBe("24.12.2020");
  });
});

function datepickerLocalisation() {
  return {
    locale: "de",
    buttonLabel: "buttonLabel-message",
    placeholder: "placeholder-message",
    selectedDateMessage: "selectedDateMessage-message",
    prevMonthLabel: "prevMonthLabel-message",
    nextMonthLabel: "nextMonthLabel-message",
    monthSelectLabel: "monthSelectLabel-message",
    yearSelectLabel: "yearSelectLabel-message",
    closeLabel: "closeLabel-message",
    keyboardInstruction: "keyboardInstruction-message",
    calendarHeading: "calendarHeading-message",
    dayNames: ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"],
    monthNames: [
      "january",
      "february",
      "march",
      "april",
      "may",
      "june",
      "july",
      "august",
      "september",
      "october",
      "november",
      "december",
    ],
    monthNamesShort: [
      "january.short",
      "february.short",
      "march.short",
      "april.short",
      "may.short",
      "june.short",
      "july.short",
      "august.short",
      "september.short",
      "october.short",
      "november.short",
      "december.short",
    ],
  };
}

function renderCurrentDatepickerMonth() {
  // fetch public holidays and update view
  document.querySelector("button.duet-date__toggle").click();

  // wait for response and css class calculation
  return new Promise((resolve) => setTimeout(resolve, 0));
}

function getDatepickerDayElement(dateString) {
  for (let span of document.querySelectorAll(".duet-date__vhidden")) {
    if (span.textContent === dateString) {
      return span.parentNode;
    }
  }
  throw new Error("could not find date element for dateString=" + dateString);
}

function fireEvent(element, event) {
  element.dispatchEvent(event);
}

fireEvent.change = function (element) {
  fireEvent(element, new Event("change"));
};
