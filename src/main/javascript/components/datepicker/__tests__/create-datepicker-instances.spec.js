import createDatepickerInstances from "../create-datepicker-instances";
import fetchMock from "fetch-mock";

describe("create-datepicker-instances", () => {
  beforeEach(() => {
    window.uv = {
      datepicker: {
        localisation: datepickerLocalisation(),
      },
    };
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
      <input id="just-a-date-picker" />
      <input id="awesome-date-picker-1" />
      <input id="awesome-date-picker-2" />
    `;

    const selectors = ["#awesome-date-picker-1", "#awesome-date-picker-2"];
    const urlPrefix = "";
    const getPerson = () => 42;
    const onSelect = jest.fn();

    await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

    expect(document.querySelector("#just-a-date-picker").closest("duet-date-picker")).toBeNull();
    expect(document.querySelector("#awesome-date-picker-1").closest("duet-date-picker")).toBeTruthy();
    expect(document.querySelector("#awesome-date-picker-2").closest("duet-date-picker")).toBeTruthy();
  });

  test("sets 'duet-radius' to 0", async () => {
    document.body.innerHTML = `
      <input />
    `;

    const selectors = ["input"];
    const urlPrefix = "";
    const getPerson = () => 42;
    const onSelect = jest.fn();

    await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

    expect(document.querySelector("duet-date-picker").getAttribute("style")).toBe("--duet-radius=0");
  });

  test("assigns original 'id' attribute", async () => {
    document.body.innerHTML = `
      <input id="awesome-date" />
    `;

    const selectors = ["input"];
    const urlPrefix = "";
    const getPerson = () => 42;
    const onSelect = jest.fn();

    await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

    expect(document.querySelector("duet-date-picker input").getAttribute("id")).toBe("awesome-date");
  });

  test("assigns original 'class' attribute", async () => {
    document.body.innerHTML = `
      <input class="foo bar" />
    `;

    const selectors = ["input"];
    const urlPrefix = "";
    const getPerson = () => 42;
    const onSelect = jest.fn();

    await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

    const { classList } = document.querySelector("duet-date-picker");
    expect(classList).toContain("foo");
    expect(classList).toContain("bar");
  });

  test("assigns original 'name' attribute", async () => {
    document.body.innerHTML = `
      <input name="start-date" />
    `;

    const selectors = ["input"];
    const urlPrefix = "";
    const getPerson = () => 42;
    const onSelect = jest.fn();

    await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

    expect(document.querySelector("duet-date-picker input").getAttribute("name")).toBe("start-date");
  });

  test("fails to render with preset date value but missing iso-value", async () => {
    document.body.innerHTML = `
      <input value="24.12.2020" />
    `;

    const selectors = ["input"];
    const urlPrefix = "";
    const getPerson = () => 42;
    const onSelect = jest.fn();

    await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

    expect(document.querySelector("duet-date-picker")).toBeNull();
  });

  test("assigns original 'value' attribute", async () => {
    document.body.innerHTML = `
      <input value="24.12.2020" data-iso-value="2020-12-24" />
    `;

    const selectors = ["input"];
    const urlPrefix = "";
    const getPerson = () => 42;
    const onSelect = jest.fn();

    await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

    expect(document.querySelector("duet-date-picker").getAttribute("value")).toBe("2020-12-24");
  });

  test('invokes "onSelect" when date value has been changed', async () => {
    document.body.innerHTML = `
      <input value="24.12.2020" data-iso-value="2020-12-24" />
    `;

    const selectors = ["input"];
    const urlPrefix = "";
    const getPerson = () => 42;
    const onSelect = jest.fn();

    await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

    expect(onSelect).not.toHaveBeenCalled();

    const event = new CustomEvent("duetChange");
    fireEvent(document.querySelector("duet-date-picker"), event);

    expect(onSelect).toHaveBeenCalledWith(event);
  });

  describe.each([["en"], ["de"], ["de-de"]])("with locale '%s'", (givenLanguage) => {
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

        const selectors = ["input"];
        const urlPrefix = "my-url-prefix";
        const getPerson = () => 42;
        const onSelect = jest.fn();

        await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

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

        const selectors = ["input"];
        const urlPrefix = "my-url-prefix";
        const getPerson = () => 42;
        const onSelect = jest.fn();

        await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

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

        assertWeekend("05.12.2020");
        assertWeekend("06.12.2020");
        assertWeekend("12.12.2020");
        assertWeekend("13.12.2020");
        assertWeekend("19.12.2020");
        assertWeekend("20.12.2020");
        assertWeekend("26.12.2020");
        assertWeekend("27.12.2020");
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

          const selectors = ["input"];
          const urlPrefix = "my-url-prefix";
          const getPerson = () => 42;
          const onSelect = jest.fn();

          await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);
        });

        test("full", async () => {
          mockPublicHolidays([
            {
              date: "2020-12-24",
              absencePeriodName: "FULL",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24.12.2020");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).toContain("datepicker-day-public-holiday-full");
          expect(element.classList).not.toContain("datepicker-day-public-holiday-morning");
          expect(element.classList).not.toContain("datepicker-day-public-holiday-noon");
        });

        test("morning", async () => {
          mockPublicHolidays([
            {
              date: "2020-12-24",
              absencePeriodName: "MORNING",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24.12.2020");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).not.toContain("datepicker-day-public-holiday-full");
          expect(element.classList).toContain("datepicker-day-public-holiday-morning");
          expect(element.classList).not.toContain("datepicker-day-public-holiday-noon");
        });

        test("noon", async () => {
          mockPublicHolidays([
            {
              date: "2020-12-24",
              absencePeriodName: "NOON",
            },
          ]);

          await renderCurrentDatepickerMonth();

          const element = getDatepickerDayElement("24.12.2020");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).not.toContain("datepicker-day-public-holiday-full");
          expect(element.classList).not.toContain("datepicker-day-public-holiday-morning");
          expect(element.classList).toContain("datepicker-day-public-holiday-noon");
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

          const selectors = ["input"];
          const urlPrefix = "my-url-prefix";
          const getPerson = () => 42;
          const onSelect = jest.fn();

          await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);
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

          const element = getDatepickerDayElement("24.12.2020");
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

          const element = getDatepickerDayElement("24.12.2020");
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

          const element = getDatepickerDayElement("24.12.2020");
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

          const selectors = ["input"];
          const urlPrefix = "my-url-prefix";
          const getPerson = () => 42;
          const onSelect = jest.fn();

          await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);
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

          const element = getDatepickerDayElement("24.12.2020");
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

          const element = getDatepickerDayElement("24.12.2020");
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

          const element = getDatepickerDayElement("24.12.2020");
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

          const selectors = ["input"];
          const urlPrefix = "my-url-prefix";
          const getPerson = () => 42;
          const onSelect = jest.fn();

          await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);
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

          const element = getDatepickerDayElement("24.12.2020");
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

          const element = getDatepickerDayElement("24.12.2020");
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

          const element = getDatepickerDayElement("24.12.2020");
          expect(element.classList).toContain("datepicker-day");
          expect(element.classList).not.toContain("datepicker-day-sick-note-full");
          expect(element.classList).not.toContain("datepicker-day-sick-note-morning");
          expect(element.classList).toContain("datepicker-day-sick-note-noon");
        });
      });
    });
  });

  test.each([["en"], ["de"]])("formats date with 'dd.MM.yyyy' for locale=%s", async (givenLanguage) => {
    jest.spyOn(window.navigator, "language", "get").mockReturnValue(givenLanguage);

    document.body.innerHTML = `
      <input value="24.12.2020" data-iso-value="2020-12-24" />
    `;

    const selectors = ["input"];
    const urlPrefix = "my-url-prefix";
    const getPerson = () => 42;
    const onSelect = jest.fn();

    await createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect);

    expect(document.querySelector("input").value).toBe("24.12.2020");
  });
});

function datepickerLocalisation() {
  return {
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
