import { prefersReducedMotion, slideFrom } from "../motion";

describe("motion", () => {
  function mockReducedMotion(reduce) {
    globalThis.matchMedia = vi.fn((query) => ({
      matches: reduce && query === "(prefers-reduced-motion: reduce)",
    }));
  }

  afterEach(() => {
    delete globalThis.matchMedia;
  });

  describe("prefersReducedMotion", () => {
    it("is true when reduced motion is preferred", () => {
      mockReducedMotion(true);
      expect(prefersReducedMotion()).toBe(true);
    });

    it("is false when reduced motion is not preferred", () => {
      mockReducedMotion(false);
      expect(prefersReducedMotion()).toBe(false);
    });

    it("follows a change of the preference", () => {
      mockReducedMotion(false);
      expect(prefersReducedMotion()).toBe(false);
      mockReducedMotion(true);
      expect(prefersReducedMotion()).toBe(true);
    });

    it("is false without matchMedia", () => {
      expect(prefersReducedMotion()).toBe(false);
    });
  });

  describe("slideFrom", () => {
    function element() {
      const div = document.createElement("div");
      div.animate = vi.fn(() => ({ cancel: vi.fn() }));
      return div;
    }

    it("slides from the offset to where the element is", () => {
      mockReducedMotion(false);
      const div = element();

      const slide = slideFrom(div, -40, 8);

      expect(div.animate).toHaveBeenCalledWith([{ translate: "-40px 8px" }, { translate: "0 0" }], {
        duration: 150,
        easing: "ease-out",
      });
      expect(slide).toBe(div.animate.mock.results[0].value);
    });

    it("slides horizontally only without a vertical offset", () => {
      mockReducedMotion(false);
      const div = element();

      slideFrom(div, 40);

      expect(div.animate).toHaveBeenCalledWith([{ translate: "40px 0px" }, { translate: "0 0" }], expect.anything());
    });

    it("does not slide when reduced motion is preferred", () => {
      mockReducedMotion(true);
      const div = element();

      expect(slideFrom(div, 40)).toBeUndefined();
      expect(div.animate).not.toHaveBeenCalled();
    });
  });
});
