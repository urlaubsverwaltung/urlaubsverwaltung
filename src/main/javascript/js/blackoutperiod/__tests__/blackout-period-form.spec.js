import { createDatepicker } from "../../../components/datepicker";

vi.mock("../../../components/datepicker", () => ({
  createDatepicker: vi.fn(),
}));

describe("blackout-period-form", () => {
  beforeEach(() => {
    vi.resetModules();
    createDatepicker.mockClear();

    globalThis.uv = { apiPrefix: "api-prefix" };

    document.body.innerHTML = `
      <input id="startDate" />
      <input id="endDate" />
    `;
  });

  afterEach(() => {
    document.body.innerHTML = "";
    delete globalThis.uv;
  });

  it("creates a datepicker for the start date and the end date", async () => {
    await import("../blackout-period-form.js");

    expect(createDatepicker).not.toHaveBeenCalled();

    document.dispatchEvent(new Event("DOMContentLoaded"));

    expect(createDatepicker).toHaveBeenCalledTimes(2);
    expect(createDatepicker).toHaveBeenNthCalledWith(
      1,
      "#startDate",
      expect.objectContaining({ urlPrefix: "api-prefix" }),
    );
    expect(createDatepicker).toHaveBeenNthCalledWith(
      2,
      "#endDate",
      expect.objectContaining({ urlPrefix: "api-prefix" }),
    );
  });

  it("does not resolve a person since blackout periods are not tied to a person", async () => {
    await import("../blackout-period-form.js");

    document.dispatchEvent(new Event("DOMContentLoaded"));

    const [, options] = createDatepicker.mock.calls[0];
    expect(options.getPersonId()).toBeUndefined();
  });
});
