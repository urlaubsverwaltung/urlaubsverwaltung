describe("back-button", function () {
  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.resetModules();
    vi.restoreAllMocks();
  });

  it("navigates back in history when clicked", async function () {
    document.body.innerHTML = `<button data-back-button>back</button>`;

    const historyBackSpy = vi.spyOn(parent.history, "back").mockImplementation(() => {});

    await import("../index");

    document.querySelector("[data-back-button]").click();

    expect(historyBackSpy).toHaveBeenCalledTimes(1);
  });

  it("does not throw when no back button exists", async function () {
    document.body.innerHTML = `<div></div>`;

    await expect(import("../index")).resolves.not.toThrow();
  });
});
