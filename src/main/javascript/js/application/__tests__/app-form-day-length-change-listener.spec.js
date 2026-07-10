import { parseISO } from "date-fns";
import sendGetDaysRequest from "../../send-get-days-request";
import sendGetDepartmentVacationsRequest from "../../send-get-department-vacations-request";

vi.mock("../../send-get-days-request", () => ({ default: vi.fn() }));
vi.mock("../../send-get-department-vacations-request", () => ({ default: vi.fn() }));

describe("app-form-day-length-change-listener", function () {
  beforeAll(async function () {
    await import("../app-form-day-length-change-listener");
  });

  beforeEach(function () {
    globalThis.uv = { apiPrefix: "/api" };
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.clearAllMocks();
  });

  function domContentLoaded() {
    document.dispatchEvent(new Event("DOMContentLoaded", { bubbles: true, cancelable: true }));
  }

  function renderForm({ from = "2024-05-10", to = "2024-05-12" } = {}) {
    document.body.innerHTML = `
      <input name="person" value="42" />
      <input type="radio" name="dayLength" value="FULL" checked />
      <input type="radio" name="dayLength" value="MORNING" />
      <duet-date-picker>
        <input id="from" />
      </duet-date-picker>
      <duet-date-picker>
        <input id="to" />
      </duet-date-picker>
      <div class="days"></div>
      <div id="departmentVacations"></div>
    `;
    document.querySelector("#from").closest("duet-date-picker").value = from;
    document.querySelector("#to").closest("duet-date-picker").value = to;
  }

  function changeDayLength(value) {
    const radio = document.querySelector(`input[name='dayLength'][value='${value}']`);
    radio.checked = true;
    radio.dispatchEvent(new Event("change", { bubbles: true }));
  }

  it("re-fetches days and department vacations when the day length changes", function () {
    renderForm({ from: "2024-05-10", to: "2024-05-12" });
    domContentLoaded();

    changeDayLength("MORNING");

    expect(sendGetDaysRequest).toHaveBeenCalledWith(
      "/api",
      parseISO("2024-05-10"),
      parseISO("2024-05-12"),
      "MORNING",
      "42",
      ".days",
    );
    expect(sendGetDepartmentVacationsRequest).toHaveBeenCalledWith(
      "/api",
      parseISO("2024-05-10"),
      parseISO("2024-05-12"),
      "42",
      "#departmentVacations",
    );
  });

  it("falls back to the start date when no end date is set", function () {
    renderForm({ from: "2024-05-10", to: "" });
    domContentLoaded();

    changeDayLength("MORNING");

    expect(sendGetDaysRequest).toHaveBeenCalledWith(
      "/api",
      parseISO("2024-05-10"),
      parseISO("2024-05-10"),
      "MORNING",
      "42",
      ".days",
    );
  });

  it("does nothing when no start date is set yet", function () {
    renderForm({ from: "", to: "2024-05-12" });
    domContentLoaded();

    changeDayLength("MORNING");

    expect(sendGetDaysRequest).not.toHaveBeenCalled();
    expect(sendGetDepartmentVacationsRequest).not.toHaveBeenCalled();
  });

  it("attaches a listener to every dayLength radio button", function () {
    renderForm();
    domContentLoaded();

    changeDayLength("FULL");
    changeDayLength("MORNING");

    expect(sendGetDaysRequest).toHaveBeenCalledTimes(2);
  });
});
