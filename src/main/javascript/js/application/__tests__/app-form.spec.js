import { createDatepicker } from "../../../components/datepicker";
import sendGetDaysRequest from "../../send-get-days-request";
import sendGetDepartmentVacationsRequest from "../../send-get-department-vacations-request";

vi.mock("../../../components/datepicker", () => ({
  createDatepicker: vi.fn((selector) => Promise.resolve(document.querySelector(selector))),
}));
vi.mock("../../send-get-days-request", () => ({ default: vi.fn() }));
vi.mock("../../send-get-department-vacations-request", () => ({ default: vi.fn() }));

describe("app-form", function () {
  beforeAll(async function () {
    await import("../app-form");
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.clearAllMocks();
    history.pushState({}, "", "/web/application/new");
  });

  function flushMicrotasks() {
    return new Promise((resolve) => setTimeout(resolve, 0));
  }

  function render() {
    document.body.innerHTML = `
      <select id="person-select">
        <option value="1">Person 1</option>
        <option value="2" selected>Person 2</option>
      </select>
      <input name="person" value="2" />
      <input name="dayLength" value="FULL" />
      <input id="from" />
      <input id="to" />
      <input id="at" />
      <div class="days"></div>
      <div id="departmentVacations"></div>
      <form>
        <button id="apply-application" type="submit">Apply</button>
      </form>
    `;
  }

  async function renderAndLoad() {
    render();
    globalThis.uv = { apiPrefix: "/api" };
    document.dispatchEvent(new Event("DOMContentLoaded", { bubbles: true, cancelable: true }));
    await flushMicrotasks();
  }

  it("creates datepickers for from/to/at with the current urlPrefix/personId", async function () {
    await renderAndLoad();

    expect(createDatepicker).toHaveBeenCalledTimes(3);
    const [fromArguments, toArguments, atArguments] = createDatepicker.mock.calls;
    expect(fromArguments[0]).toBe("#from");
    expect(fromArguments[1].urlPrefix).toBe("/api");
    expect(fromArguments[1].getPersonId()).toBe("2");
    expect(toArguments[0]).toBe("#to");
    expect(atArguments[0]).toBe("#at");
  });

  it("navigating via #person-select does not throw", async function () {
    await renderAndLoad();

    expect(() => {
      const select = document.querySelector("#person-select");
      select.value = "1";
      select.dispatchEvent(new Event("change", { bubbles: true }));
    }).not.toThrow();
  });

  describe("selecting a 'from' date", function () {
    it("defaults 'to' to the same value and fetches days/department vacations", async function () {
      await renderAndLoad();
      const [fromArguments] = createDatepicker.mock.calls;

      document.querySelector("#from").value = "2024-05-10";
      fromArguments[1].onSelect();

      expect(document.querySelector("#to").value).toBe("2024-05-10");
      expect(sendGetDaysRequest).toHaveBeenCalledWith(
        "/api",
        parseISOLocal("2024-05-10"),
        parseISOLocal("2024-05-10"),
        "FULL",
        "2",
        ".days",
      );
      expect(sendGetDepartmentVacationsRequest).toHaveBeenCalledWith(
        "/api",
        parseISOLocal("2024-05-10"),
        parseISOLocal("2024-05-10"),
        "2",
        "#departmentVacations",
      );
    });

    it("does not override an already set 'to' date", async function () {
      await renderAndLoad();
      const [fromArguments] = createDatepicker.mock.calls;
      document.querySelector("#to").value = "2024-06-01";

      document.querySelector("#from").value = "2024-05-10";
      fromArguments[1].onSelect();

      expect(document.querySelector("#to").value).toBe("2024-06-01");
    });

    it("does not fetch anything when only 'from' has a value and 'to' is cleared", async function () {
      await renderAndLoad();
      const [fromArguments] = createDatepicker.mock.calls;
      document.querySelector("#to").value = "";

      document.querySelector("#from").value = "";
      fromArguments[1].onSelect();

      expect(sendGetDaysRequest).not.toHaveBeenCalled();
    });
  });

  describe("selecting a 'to' date", function () {
    it("fetches days/department vacations using the current from/to values", async function () {
      await renderAndLoad();
      const [, toArguments] = createDatepicker.mock.calls;
      document.querySelector("#from").value = "2024-05-10";
      document.querySelector("#to").value = "2024-05-12";

      toArguments[1].onSelect();

      expect(sendGetDaysRequest).toHaveBeenCalledWith(
        "/api",
        parseISOLocal("2024-05-10"),
        parseISOLocal("2024-05-12"),
        "FULL",
        "2",
        ".days",
      );
    });

    it("does not fetch anything while 'from' is still empty", async function () {
      await renderAndLoad();
      const [, toArguments] = createDatepicker.mock.calls;
      document.querySelector("#to").value = "2024-05-12";

      toArguments[1].onSelect();

      expect(sendGetDaysRequest).not.toHaveBeenCalled();
    });
  });

  describe("selecting an 'at' date", function () {
    it("refreshes hints using from/to, ignoring the 'at' value itself", async function () {
      await renderAndLoad();
      const atArguments = createDatepicker.mock.calls[2];
      document.querySelector("#from").value = "2024-05-10";
      document.querySelector("#to").value = "2024-05-12";
      document.querySelector("#at").value = "2024-05-11";

      atArguments[1].onSelect();

      expect(sendGetDaysRequest).toHaveBeenCalledWith(
        "/api",
        parseISOLocal("2024-05-10"),
        parseISOLocal("2024-05-12"),
        "FULL",
        "2",
        ".days",
      );
    });
  });

  describe("preset dates from the URL (calendar day click)", function () {
    it("fetches days/department vacations for 'from' and 'to' query params", async function () {
      history.pushState({}, "", "/web/application/new?from=2024-05-10&to=2024-05-12");

      await renderAndLoad();

      expect(sendGetDaysRequest).toHaveBeenCalledWith(
        "/api",
        parseISOLocal("2024-05-10"),
        parseISOLocal("2024-05-12"),
        "FULL",
        "2",
        ".days",
      );
      expect(sendGetDepartmentVacationsRequest).toHaveBeenCalledWith(
        "/api",
        parseISOLocal("2024-05-10"),
        parseISOLocal("2024-05-12"),
        "2",
        "#departmentVacations",
      );
    });

    it("defaults 'to' to 'from' when only 'from' is present in the URL", async function () {
      history.pushState({}, "", "/web/application/new?from=2024-05-10");

      await renderAndLoad();

      expect(sendGetDaysRequest).toHaveBeenCalledWith(
        "/api",
        parseISOLocal("2024-05-10"),
        parseISOLocal("2024-05-10"),
        "FULL",
        "2",
        ".days",
      );
    });

    it("does not fetch anything when there is no 'from' query param", async function () {
      await renderAndLoad();

      expect(sendGetDaysRequest).not.toHaveBeenCalled();
    });
  });

  describe("#apply-application submit guard", function () {
    it("submits the form on the first click and prevents the default action", async function () {
      await renderAndLoad();
      const button = document.querySelector("#apply-application");
      button.form.submit = vi.fn();

      const event = new MouseEvent("click", { bubbles: true, cancelable: true });
      button.dispatchEvent(event);

      expect(event.defaultPrevented).toBe(true);
      expect(button.form.submit).toHaveBeenCalledTimes(1);
    });

    it("ignores subsequent clicks after the first submit", async function () {
      await renderAndLoad();
      const button = document.querySelector("#apply-application");
      button.form.submit = vi.fn();

      button.dispatchEvent(new MouseEvent("click", { bubbles: true, cancelable: true }));
      button.dispatchEvent(new MouseEvent("click", { bubbles: true, cancelable: true }));

      expect(button.form.submit).toHaveBeenCalledTimes(1);
    });
  });
});

function parseISOLocal(dateString) {
  const [year, month, day] = dateString.split("-").map(Number);
  return new Date(year, month - 1, day);
}
