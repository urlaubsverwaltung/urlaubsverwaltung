import { prepareTooltip, setup, teardown } from "./tooltip";

describe("tooltip", function () {
  beforeEach(function () {
    vi.useFakeTimers();
    globalThis.matchMedia = vi.fn(function (query) {
      return {
        matches: !query.includes("reduce"),
        addEventListener() {},
        removeEventListener() {},
      };
    });
    setup();
  });

  afterEach(function () {
    teardown();
    vi.useRealTimers();
    document.body.replaceChildren();
  });

  function sut() {
    return document.querySelector("#uv-tooltip");
  }

  function spyOnPopover(element) {
    element.showPopover = vi.fn();
    element.hidePopover = vi.fn();
    element.animate = vi.fn(function () {
      return { cancel: vi.fn(), finished: Promise.resolve() };
    });
    return { show: element.showPopover, hide: element.hidePopover, animate: element.animate };
  }

  function anchorWith(title, ...children) {
    const button = document.createElement("button");
    button.setAttribute("title", title);
    button.append(...children);
    return button;
  }

  test("mouseover on a tooltip anchor shows the tooltip after the 300ms hover delay", function () {
    const button = anchorWith("Edit user");
    document.body.append(button);

    const { show } = spyOnPopover(sut());

    button.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
    vi.advanceTimersByTime(299);
    expect(show).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    expect(show).toHaveBeenCalledOnce();
    expect(button.classList.contains("uv-tooltip-anchor--active")).toBe(true);
    expect(button.getAttribute("aria-describedby")).toBe("uv-tooltip");
    expect(sut().textContent).toBe("Edit user");
    expect(button.hasAttribute("title")).toBe(false);
    expect(button.dataset.title).toBe("Edit user");
  });

  test("focusin on a tooltip anchor shows the tooltip immediately (0ms delay)", function () {
    const button = anchorWith("Submit");
    document.body.append(button);

    const { show } = spyOnPopover(sut());

    button.dispatchEvent(new FocusEvent("focusin", { bubbles: true }));

    expect(show).toHaveBeenCalledOnce();
    expect(button.classList.contains("uv-tooltip-anchor--active")).toBe(true);
    expect(button.getAttribute("aria-describedby")).toBe("uv-tooltip");
    expect(sut().textContent).toBe("Submit");
  });

  test("mouseover honors a custom data-tooltip-delay over the default 300ms", function () {
    const button = anchorWith("Edit user");
    button.dataset.tooltipDelay = "800";
    document.body.append(button);

    const { show } = spyOnPopover(sut());

    button.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
    vi.advanceTimersByTime(799);
    expect(show).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    expect(show).toHaveBeenCalledOnce();
  });

  test("mouseover with data-tooltip-delay=0 shows the tooltip immediately", function () {
    const button = anchorWith("Edit user");
    button.dataset.tooltipDelay = "0";
    document.body.append(button);

    const { show } = spyOnPopover(sut());

    button.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
    vi.advanceTimersByTime(0);

    expect(show).toHaveBeenCalledOnce();
  });

  test("focusin shows immediately even when a custom data-tooltip-delay is set", function () {
    const button = anchorWith("Submit");
    button.dataset.tooltipDelay = "800";
    document.body.append(button);

    const { show } = spyOnPopover(sut());

    button.dispatchEvent(new FocusEvent("focusin", { bubbles: true }));

    expect(show).toHaveBeenCalledOnce();
  });

  test("mouseover on another tooltip anchor while open retargets instantly without re-opening", function () {
    const first = anchorWith("First");
    const second = anchorWith("Second");
    document.body.append(first, second);

    const { show, hide } = spyOnPopover(sut());

    first.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
    vi.advanceTimersByTime(300);
    expect(show).toHaveBeenCalledOnce();

    second.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));

    expect(show).toHaveBeenCalledOnce();
    expect(hide).not.toHaveBeenCalled();
    expect(first.classList.contains("uv-tooltip-anchor--active")).toBe(false);
    expect(first.hasAttribute("aria-describedby")).toBe(false);
    expect(second.classList.contains("uv-tooltip-anchor--active")).toBe(true);
    expect(second.getAttribute("aria-describedby")).toBe("uv-tooltip");
    expect(sut().textContent).toBe("Second");
  });

  test("mouseout from an open tooltip anchor delays hidePopover until the fade-out completes", function () {
    const button = anchorWith("Edit user");
    document.body.append(button);

    const { hide } = spyOnPopover(sut());

    button.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
    vi.advanceTimersByTime(300);

    button.dispatchEvent(new MouseEvent("mouseout", { bubbles: true }));

    expect(hide).not.toHaveBeenCalled();
    expect(sut().classList.contains("uv-tooltip--is-hiding")).toBe(true);
    expect(button.classList.contains("uv-tooltip-anchor--active")).toBe(true);
    expect(button.getAttribute("aria-describedby")).toBe("uv-tooltip");

    vi.advanceTimersByTime(100);

    expect(hide).toHaveBeenCalledOnce();
    expect(sut().classList.contains("uv-tooltip--is-hiding")).toBe(false);
    expect(button.classList.contains("uv-tooltip-anchor--active")).toBe(false);
    expect(button.hasAttribute("aria-describedby")).toBe(false);
  });

  test("mouseover on another tooltip anchor during pending-hide cancels the hide without toggling popover", function () {
    const first = anchorWith("First");
    const second = anchorWith("Second");
    document.body.append(first, second);

    const { show, hide } = spyOnPopover(sut());

    first.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
    vi.advanceTimersByTime(300);
    expect(show).toHaveBeenCalledTimes(1);

    first.dispatchEvent(new MouseEvent("mouseout", { bubbles: true }));
    expect(hide).not.toHaveBeenCalled();
    expect(sut().classList.contains("uv-tooltip--is-hiding")).toBe(true);

    vi.advanceTimersByTime(50);
    second.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));

    expect(show).toHaveBeenCalledTimes(1);
    expect(hide).not.toHaveBeenCalled();
    expect(sut().classList.contains("uv-tooltip--is-hiding")).toBe(false);
    expect(first.classList.contains("uv-tooltip-anchor--active")).toBe(false);
    expect(first.hasAttribute("aria-describedby")).toBe(false);
    expect(second.classList.contains("uv-tooltip-anchor--active")).toBe(true);
    expect(second.getAttribute("aria-describedby")).toBe("uv-tooltip");
    expect(sut().textContent).toBe("Second");

    vi.advanceTimersByTime(100);
    expect(hide).not.toHaveBeenCalled();
    expect(second.classList.contains("uv-tooltip-anchor--active")).toBe(true);
  });

  test("mouseover on an element without title or data-title is a no-op", function () {
    const plain = document.createElement("div");
    document.body.append(plain);

    const { show } = spyOnPopover(sut());

    plain.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
    vi.advanceTimersByTime(1000);

    expect(show).not.toHaveBeenCalled();
    expect(plain.classList.contains("uv-tooltip-anchor--active")).toBe(false);
    expect(plain.hasAttribute("aria-describedby")).toBe(false);
  });

  test("mouseover events bubbling from child elements of the same anchor do not restart the show timer", function () {
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    const button = anchorWith("Edit", svg);
    document.body.append(button);

    const { show } = spyOnPopover(sut());

    button.dispatchEvent(new MouseEvent("mouseover", { bubbles: true, relatedTarget: document.body }));
    vi.advanceTimersByTime(200);

    svg.dispatchEvent(new MouseEvent("mouseover", { bubbles: true, relatedTarget: button }));
    vi.advanceTimersByTime(100);

    expect(show).toHaveBeenCalledOnce();
  });

  test("leaving a tooltip anchor before its show-delay elapses cancels the pending show", function () {
    const first = anchorWith("First");
    const second = anchorWith("Second");
    const third = anchorWith("Third");
    document.body.append(first, second, third);

    const { show } = spyOnPopover(sut());

    first.dispatchEvent(new MouseEvent("mouseover", { bubbles: true, relatedTarget: document.body }));
    vi.advanceTimersByTime(100);

    first.dispatchEvent(new MouseEvent("mouseout", { bubbles: true, relatedTarget: second }));
    second.dispatchEvent(new MouseEvent("mouseover", { bubbles: true, relatedTarget: first }));
    vi.advanceTimersByTime(100);

    second.dispatchEvent(new MouseEvent("mouseout", { bubbles: true, relatedTarget: third }));
    third.dispatchEvent(new MouseEvent("mouseover", { bubbles: true, relatedTarget: second }));
    vi.advanceTimersByTime(100);

    third.dispatchEvent(new MouseEvent("mouseout", { bubbles: true, relatedTarget: document.body }));
    vi.advanceTimersByTime(1000);

    expect(show).not.toHaveBeenCalled();
  });

  test("handoff to another anchor animates the tooltip slide via WAAPI", function () {
    const first = anchorWith("First");
    const second = anchorWith("Second");
    document.body.append(first, second);

    const { animate } = spyOnPopover(sut());

    first.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
    vi.advanceTimersByTime(300);
    expect(animate).not.toHaveBeenCalled();

    second.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));

    expect(animate).toHaveBeenCalledOnce();
    const [keyframes, options] = animate.mock.calls[0];
    expect(keyframes).toHaveLength(2);
    expect(keyframes[0]).toHaveProperty("translate");
    expect(keyframes[1]).toEqual({ translate: "0 0" });
    expect(options).toMatchObject({ duration: 150, easing: "ease-out" });
  });

  test("handoff does not animate when prefers-reduced-motion is reduce", function () {
    globalThis.matchMedia = vi.fn(function (query) {
      return { matches: query.includes("reduce"), addEventListener() {}, removeEventListener() {} };
    });
    teardown();
    setup();

    const first = anchorWith("First");
    const second = anchorWith("Second");
    document.body.append(first, second);

    const { animate } = spyOnPopover(sut());

    first.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
    vi.advanceTimersByTime(300);
    second.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));

    expect(animate).not.toHaveBeenCalled();
    expect(second.classList.contains("uv-tooltip-anchor--active")).toBe(true);
  });

  test("prepareTooltip writes data-tooltip-delay when a delay is given", function () {
    const button = document.createElement("button");
    document.body.append(button);

    prepareTooltip(button, { text: "Edit user", delay: 500 });

    expect(button.dataset.tooltipDelay).toBe("500");
  });

  test("prepareTooltip writes data-tooltip-placement when a placement is given", function () {
    const button = document.createElement("button");
    document.body.append(button);

    prepareTooltip(button, { text: "Edit user", placement: "right" });

    expect(button.dataset.tooltipPlacement).toBe("right");
  });

  test("prepareTooltip leaves data-tooltip-placement untouched when placement is omitted", function () {
    const button = document.createElement("button");
    button.dataset.tooltipPlacement = "right";
    document.body.append(button);

    prepareTooltip(button, { text: "Edit user" });

    expect(button.dataset.tooltipPlacement).toBe("right");
  });

  test("showing a tooltip mirrors the anchor placement onto the tooltip element", function () {
    const button = anchorWith("Edit user");
    button.dataset.tooltipPlacement = "right";
    document.body.append(button);

    spyOnPopover(sut());

    button.dispatchEvent(new FocusEvent("focusin", { bubbles: true }));

    expect(sut().dataset.placement).toBe("right");
  });

  test("showing a tooltip without placement defaults the tooltip element to top", function () {
    const button = anchorWith("Edit user");
    document.body.append(button);

    spyOnPopover(sut());

    button.dispatchEvent(new FocusEvent("focusin", { bubbles: true }));

    expect(sut().dataset.placement).toBe("top");
  });

  test("mouseout to a child within the same anchor does not begin hide", function () {
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    const button = anchorWith("Edit", svg);
    document.body.append(button);

    const { hide } = spyOnPopover(sut());

    button.dispatchEvent(new MouseEvent("mouseover", { bubbles: true, relatedTarget: document.body }));
    vi.advanceTimersByTime(300);

    button.dispatchEvent(new MouseEvent("mouseout", { bubbles: true, relatedTarget: svg }));
    vi.advanceTimersByTime(100);

    expect(hide).not.toHaveBeenCalled();
    expect(sut().classList.contains("uv-tooltip--is-hiding")).toBe(false);
    expect(button.classList.contains("uv-tooltip-anchor--active")).toBe(true);
  });
});
