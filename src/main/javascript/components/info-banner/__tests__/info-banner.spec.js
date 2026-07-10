describe("info-banner", function () {
  beforeEach(function () {
    globalThis.injectStyle = vi.fn((css) => {
      const style = document.createElement("style");
      style.textContent = css;
      document.head.append(style);
      return style;
    });
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    while (document.head.firstElementChild) {
      document.head.firstElementChild.remove();
    }
    delete globalThis.injectStyle;
    vi.resetModules();
  });

  it("injects a height of 0 when there is no #info-banner element", async function () {
    document.body.innerHTML = ``;

    await import("../info-banner");

    expect(globalThis.injectStyle).toHaveBeenCalledWith(":root { --info-banner-height: 0px; }");
  });

  it("injects the current height of the #info-banner element", async function () {
    document.body.innerHTML = `<div id="info-banner"></div>`;
    vi.spyOn(document.querySelector("#info-banner"), "getBoundingClientRect").mockReturnValue({ height: 42 });

    await import("../info-banner");

    expect(globalThis.injectStyle).toHaveBeenCalledWith(":root { --info-banner-height: 42px; }");
  });

  it("recalculates and updates the injected style on window resize", async function () {
    document.body.innerHTML = `<div id="info-banner"></div>`;
    const banner = document.querySelector("#info-banner");
    const getBoundingClientRectSpy = vi.spyOn(banner, "getBoundingClientRect").mockReturnValue({ height: 42 });

    await import("../info-banner");
    const style = globalThis.injectStyle.mock.results[0].value;

    expect(style.textContent).toBe(":root { --info-banner-height: 42px; }");

    getBoundingClientRectSpy.mockReturnValue({ height: 84 });
    window.dispatchEvent(new Event("resize"));

    expect(style.textContent).toBe(":root { --info-banner-height: 84px; }");
  });
});
