class FakeIntersectionObserver {
  static instances = [];

  constructor(callback, options) {
    this.callback = callback;
    this.options = options;
    this.observed = new Set();
    FakeIntersectionObserver.instances.push(this);
  }

  observe(target) {
    this.observed.add(target);
  }

  unobserve(target) {
    this.observed.delete(target);
  }

  disconnect() {
    this.observed.clear();
  }

  /** test helper: simulate the browser reporting a new intersection ratio */
  trigger(target, intersectionRatio) {
    this.callback([{ target, intersectionRatio }]);
  }
}

describe("sticky", function () {
  beforeAll(async function () {
    globalThis.IntersectionObserver = FakeIntersectionObserver;
    await import("../sticky");
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    FakeIntersectionObserver.instances = [];
  });

  function renderSticky(attrs = "") {
    document.body.innerHTML = `<uv-sticky ${attrs}></uv-sticky>`;
    return document.querySelector("uv-sticky");
  }

  it("sets its position and z-index on creation", function () {
    const sticky = renderSticky();

    expect(sticky.style.position).toBe("sticky");
    expect(sticky.style.zIndex).toBe("var(--z-index-sticky)");
  });

  it("observes itself via IntersectionObserver on connect", function () {
    const sticky = renderSticky();

    const [observer] = FakeIntersectionObserver.instances;
    expect(observer.observed.has(sticky)).toBe(true);
  });

  it("unobserves itself when removed from the DOM", function () {
    const sticky = renderSticky();
    const [observer] = FakeIntersectionObserver.instances;

    sticky.remove();

    expect(observer.observed.has(sticky)).toBe(false);
  });

  it("adds the 'stuck' class once it is no longer fully intersecting", function () {
    const sticky = renderSticky();
    const [observer] = FakeIntersectionObserver.instances;

    observer.trigger(sticky, 0.5);

    expect(sticky.classList.contains("uv-sticky--stuck")).toBe(true);
  });

  it("removes the 'stuck' class once fully intersecting again", function () {
    const sticky = renderSticky();
    const [observer] = FakeIntersectionObserver.instances;

    observer.trigger(sticky, 0.5);
    observer.trigger(sticky, 1);

    expect(sticky.classList.contains("uv-sticky--stuck")).toBe(false);
  });

  describe("data-sticky attribute", function () {
    it("positions the element at the top", function () {
      const sticky = renderSticky(`data-sticky="top"`);

      expect(sticky.style.top).toBe("-1px");
      expect(sticky.style.bottom).toBe("");
    });

    it("positions the element at the bottom", function () {
      const sticky = renderSticky(`data-sticky="bottom"`);

      expect(sticky.style.bottom).toBe("-1px");
      expect(sticky.style.top).toBe("");
    });

    it("switching from bottom to top clears the bottom offset", function () {
      const sticky = renderSticky(`data-sticky="bottom"`);

      sticky.setAttribute("data-sticky", "top");

      expect(sticky.style.top).toBe("-1px");
      expect(sticky.style.bottom).toBe("");
    });

    it("clears both offsets for any other value", function () {
      const sticky = renderSticky(`data-sticky="top"`);

      sticky.removeAttribute("data-sticky");

      expect(sticky.style.top).toBe("");
      expect(sticky.style.bottom).toBe("");
    });
  });

  it("does not re-run connectedCallback logic on connectedMoveCallback", function () {
    const sticky = renderSticky();
    const observeSpy = vi.spyOn(FakeIntersectionObserver.instances[0], "observe");

    sticky.connectedMoveCallback();

    expect(observeSpy).not.toHaveBeenCalled();
  });
});
