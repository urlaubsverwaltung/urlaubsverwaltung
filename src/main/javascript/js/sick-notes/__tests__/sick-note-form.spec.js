import { createDatepicker } from "../../../components/datepicker";

vi.mock("../../../components/datepicker", () => ({
  createDatepicker: vi.fn((selector) => Promise.resolve(document.querySelector(selector))),
}));

describe("sick-note-form", function () {
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
      <input id="from" />
      <input id="to" />
      <input id="aubFrom" />
      <input id="aubTo" />
    `;
    globalThis.uv = { apiPrefix: "/api" };

    await import("../sick-note-form");
    document.dispatchEvent(new Event("DOMContentLoaded", { bubbles: true, cancelable: true }));
    await flushMicrotasks();
  }

  it("creates datepickers for from/to/aubFrom/aubTo with the current urlPrefix/personId", async function () {
    await renderAndLoad();

    expect(createDatepicker).toHaveBeenCalledTimes(4);
    const [fromArguments, toArguments, aubFromArguments, aubToArguments] = createDatepicker.mock.calls;
    expect(fromArguments[0]).toBe("#from");
    expect(fromArguments[1].urlPrefix).toBe("/api");
    expect(fromArguments[1].getPersonId()).toBe("7");
    expect(toArguments[0]).toBe("#to");
    expect(aubFromArguments[0]).toBe("#aubFrom");
    expect(aubToArguments[0]).toBe("#aubTo");
  });

  it("does not attach onSelect handlers to the 'to' or 'aubTo' pickers", async function () {
    await renderAndLoad();

    const [, toArguments, , aubToArguments] = createDatepicker.mock.calls;
    expect(toArguments[1].onSelect).toBeUndefined();
    expect(aubToArguments[1].onSelect).toBeUndefined();
  });

  it("defaults 'to' to the selected 'from' date when 'to' is still empty", async function () {
    await renderAndLoad();
    const [fromArguments] = createDatepicker.mock.calls;

    document.querySelector("#from").value = "2024-05-10";
    fromArguments[1].onSelect();

    expect(document.querySelector("#to").value).toBe("2024-05-10");
  });

  it("does not override an already set 'to' date", async function () {
    await renderAndLoad();
    const [fromArguments] = createDatepicker.mock.calls;
    document.querySelector("#to").value = "2024-06-01";

    document.querySelector("#from").value = "2024-05-10";
    fromArguments[1].onSelect();

    expect(document.querySelector("#to").value).toBe("2024-06-01");
  });

  it("defaults 'aubTo' to the selected 'aubFrom' date when 'aubTo' is still empty", async function () {
    await renderAndLoad();
    const aubFromArguments = createDatepicker.mock.calls[2];

    document.querySelector("#aubFrom").value = "2024-05-15";
    aubFromArguments[1].onSelect();

    expect(document.querySelector("#aubTo").value).toBe("2024-05-15");
  });

  it("does not override an already set 'aubTo' date", async function () {
    await renderAndLoad();
    const aubFromArguments = createDatepicker.mock.calls[2];
    document.querySelector("#aubTo").value = "2024-06-20";

    document.querySelector("#aubFrom").value = "2024-05-15";
    aubFromArguments[1].onSelect();

    expect(document.querySelector("#aubTo").value).toBe("2024-06-20");
  });
});
