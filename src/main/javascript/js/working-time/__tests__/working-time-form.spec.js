import { createDatepicker } from "../../../components/datepicker";

vi.mock("../../../components/datepicker", () => ({ createDatepicker: vi.fn() }));

describe("working-time-form", function () {
  afterEach(function () {
    vi.clearAllMocks();
    vi.resetModules();
  });

  it("creates a datepicker for #validFrom using the current uv config", async function () {
    globalThis.uv = { apiPrefix: "/api", personId: 42 };

    await import("../working-time-form");

    expect(createDatepicker).toHaveBeenCalledTimes(1);
    const [selector, options] = createDatepicker.mock.calls[0];
    expect(selector).toBe("#validFrom");
    expect(options.urlPrefix).toBe("/api");
    expect(options.getPersonId()).toBe(42);
  });

  it("getPersonId reads globalThis.uv.personId lazily", async function () {
    globalThis.uv = { apiPrefix: "/api", personId: 1 };

    await import("../working-time-form");

    const { getPersonId } = createDatepicker.mock.calls[0][1];
    globalThis.uv.personId = 99;

    expect(getPersonId()).toBe(99);
  });
});
