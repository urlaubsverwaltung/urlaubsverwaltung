import sendGetDaysRequestForTurnOfTheYear from "../../send-get-days-request-for-turn-of-the-year";

vi.mock("../../send-get-days-request-for-turn-of-the-year", () => ({ default: vi.fn() }));

describe("app-info", function () {
  beforeAll(async function () {
    await import("../app-info");
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

  beforeEach(function () {
    globalThis.uv = {
      apiPrefix: "/api",
      dayLength: "FULL",
      personId: 42,
      startDate: "2024-01-01",
      endDate: "2024-12-31",
    };
  });

  it("fetches the turn-of-the-year days when a '.days' element exists", function () {
    document.body.innerHTML = `<span class="days"></span>`;

    domContentLoaded();

    expect(sendGetDaysRequestForTurnOfTheYear).toHaveBeenCalledWith(
      "/api",
      new Date("2024-01-01"),
      new Date("2024-12-31"),
      "FULL",
      42,
      ".days",
    );
  });

  it("does nothing when there is no '.days' element", function () {
    document.body.innerHTML = ``;

    domContentLoaded();

    expect(sendGetDaysRequestForTurnOfTheYear).not.toHaveBeenCalled();
  });
});
