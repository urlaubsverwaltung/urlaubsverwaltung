import { createDatepicker } from "../../../components/datepicker";

vi.mock("../../../components/datepicker", () => ({
  createDatepicker: vi.fn((selector) => Promise.resolve(document.querySelector(selector))),
}));

describe("overtime-form", function () {
  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.clearAllMocks();
    vi.resetModules();
  });

  function flushMicrotasks() {
    return new Promise((resolve) => setTimeout(resolve, 0));
  }

  async function renderAndLoad() {
    document.body.innerHTML = `
      <input name="person" value="7" />
      <input id="startDate" />
      <input id="endDate" />
    `;
    globalThis.uv = { apiPrefix: "/api" };

    await import("../overtime-form");
    document.dispatchEvent(new Event("DOMContentLoaded", { bubbles: true, cancelable: true }));
    await flushMicrotasks();
  }

  it("creates datepickers for #startDate and #endDate with the current urlPrefix/personId", async function () {
    await renderAndLoad();

    expect(createDatepicker).toHaveBeenCalledTimes(2);
    const [startArguments, endArguments] = createDatepicker.mock.calls;
    expect(startArguments[0]).toBe("#startDate");
    expect(startArguments[1].urlPrefix).toBe("/api");
    expect(startArguments[1].getPersonId()).toBe("7");
    expect(endArguments[0]).toBe("#endDate");
    expect(endArguments[1].urlPrefix).toBe("/api");
  });

  it("does not attach an onSelect handler to the end date picker", async function () {
    await renderAndLoad();

    const [, endArguments] = createDatepicker.mock.calls;
    expect(endArguments[1].onSelect).toBeUndefined();
  });

  it("defaults the end date to the start date when the end date is still empty", async function () {
    await renderAndLoad();
    const [startArguments] = createDatepicker.mock.calls;

    document.querySelector("#startDate").value = "2024-05-10";
    startArguments[1].onSelect();

    expect(document.querySelector("#endDate").value).toBe("2024-05-10");
  });

  it("does not override an already set end date", async function () {
    await renderAndLoad();
    const [startArguments] = createDatepicker.mock.calls;
    document.querySelector("#endDate").value = "2024-06-01";

    document.querySelector("#startDate").value = "2024-05-10";
    startArguments[1].onSelect();

    expect(document.querySelector("#endDate").value).toBe("2024-06-01");
  });
});
