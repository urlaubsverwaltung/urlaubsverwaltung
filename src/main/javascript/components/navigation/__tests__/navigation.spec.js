import { patchJson } from "../../../js/fetch";
import { prepareTooltip, disposeTooltip } from "../../tooltip/tooltip";

vi.mock("../../../js/fetch", () => ({ patchJson: vi.fn() }));
vi.mock("../../tooltip/tooltip", () => ({ prepareTooltip: vi.fn(), disposeTooltip: vi.fn() }));

describe("navigation", function () {
  beforeAll(async function () {
    await import("../navigation");
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    delete document.documentElement.dataset.navCollapsed;
    vi.clearAllMocks();
  });

  function renderNav({ collapsed = false } = {}) {
    if (collapsed) {
      document.documentElement.dataset.navCollapsed = "";
    }

    document.body.innerHTML = `
      <nav class="navigation">
        <ul>
          <li>
            <a id="link1" class="navigation-link" href="#">
              <span class="nav-link-text">Link 1</span>
            </a>
          </li>
          <li>
            <button type="button" id="link2" class="navigation-link" data-href="#target2" aria-haspopup="true">
              <span class="nav-link-text">Link 2</span>
            </button>
            <ul class="subnav-group">
              <li><a id="sub1" class="navigation-sublink" href="#">Sub 1</a></li>
            </ul>
          </li>
        </ul>
        <div>
          <a id="global-help-button" class="navigation-link" href="#" target="_blank" rel="noopener">
            <span class="nav-link-text">Help</span>
          </a>
          <button
            id="nav-toggle"
            class="navigation-link"
            aria-label="Toggle navigation"
            aria-expanded="${!collapsed}"
          ></button>
        </div>
      </nav>
    `;

    return {
      link1: document.querySelector("#link1"),
      link2: document.querySelector("#link2"),
      sub1: document.querySelector("#sub1"),
      helpButton: document.querySelector("#global-help-button"),
      navToggle: document.querySelector("#nav-toggle"),
    };
  }

  function click(element, options = {}) {
    const event = new MouseEvent("click", { bubbles: true, cancelable: true, ...options });
    element.dispatchEvent(event);
    return event;
  }

  describe("nav-toggle", function () {
    it("collapses the navigation, persists it and adds tooltips", function () {
      const { link1, helpButton, navToggle } = renderNav({ collapsed: false });

      click(navToggle);

      expect(Object.hasOwn(document.documentElement.dataset, "navCollapsed")).toBe(true);
      expect(navToggle.getAttribute("aria-expanded")).toBe("false");
      expect(patchJson).toHaveBeenCalledWith("/api/persons/me/settings", { navigationCollapsed: true });

      // only links whose <li> has no subnav-group get a tooltip via the generic loop,
      // plus the help button and the toggle button always do
      expect(prepareTooltip).toHaveBeenCalledWith(link1, expect.objectContaining({ placement: "right" }));
      expect(prepareTooltip).toHaveBeenCalledWith(helpButton, expect.objectContaining({ placement: "right" }));
      expect(prepareTooltip).toHaveBeenCalledWith(navToggle, expect.objectContaining({ placement: "right" }));
      expect(prepareTooltip).toHaveBeenCalledTimes(3);
    });

    it("expands the navigation, persists it and removes tooltips", function () {
      const { link1, link2, helpButton, navToggle } = renderNav({ collapsed: true });

      click(navToggle);

      expect(Object.hasOwn(document.documentElement.dataset, "navCollapsed")).toBe(false);
      expect(navToggle.getAttribute("aria-expanded")).toBe("true");
      expect(patchJson).toHaveBeenCalledWith("/api/persons/me/settings", { navigationCollapsed: false });

      expect(disposeTooltip).toHaveBeenCalledWith(link1);
      expect(disposeTooltip).toHaveBeenCalledWith(link2);
      expect(disposeTooltip).toHaveBeenCalledWith(helpButton);
      expect(disposeTooltip).toHaveBeenCalledWith(navToggle);
    });

    it("ignores the click while a modifier key is held", function () {
      const { navToggle } = renderNav({ collapsed: false });

      click(navToggle, { shiftKey: true });

      expect(Object.hasOwn(document.documentElement.dataset, "navCollapsed")).toBe(false);
      expect(patchJson).not.toHaveBeenCalled();
    });
  });

  describe("navigation-link buttons with data-href (expandable groups)", function () {
    it("navigates and shows a loading state while expanded", function () {
      const { link2 } = renderNav({ collapsed: false });

      click(link2);

      expect(link2.classList.contains("navigation-link--loading")).toBe(true);
      expect(location.href.endsWith("#target2")).toBe(true);
    });

    it("does nothing while the navigation is collapsed", function () {
      const { link2 } = renderNav({ collapsed: true });
      const hrefBefore = location.href;

      click(link2);

      expect(link2.classList.contains("navigation-link--loading")).toBe(false);
      expect(location.href).toBe(hrefBefore);
    });
  });

  describe("plain navigation links", function () {
    it("adds a loading class to the clicked navigation-link", function () {
      const { link1 } = renderNav();

      click(link1);

      expect(link1.classList.contains("navigation-link--loading")).toBe(true);
    });

    it("adds a loading class to the clicked navigation-sublink", function () {
      const { sub1 } = renderNav();

      click(sub1);

      expect(sub1.classList.contains("navigation-sublink--loading")).toBe(true);
    });

    it("does not add a loading class when opening in a new tab", function () {
      const { helpButton } = renderNav();

      click(helpButton);

      expect(helpButton.classList.contains("navigation-link--loading")).toBe(false);
    });

    it("ignores clicks on plain buttons that are not navigation links", function () {
      const { link1 } = renderNav();
      document.body.insertAdjacentHTML("beforeend", `<button id="plain">plain</button>`);

      expect(() => click(document.querySelector("#plain"))).not.toThrow();
      expect(link1.classList.contains("navigation-link--loading")).toBe(false);
    });
  });

  describe("loading class cleanup", function () {
    it("clears loading classes on turbo:before-cache", function () {
      const { link1, sub1 } = renderNav();
      link1.classList.add("navigation-link--loading");
      sub1.classList.add("navigation-sublink--loading");

      document.dispatchEvent(new Event("turbo:before-cache", { bubbles: true }));

      expect(link1.classList.contains("navigation-link--loading")).toBe(false);
      expect(sub1.classList.contains("navigation-sublink--loading")).toBe(false);
    });

    it("clears loading classes on a persisted (bfcache) pageshow", function () {
      const { link1 } = renderNav();
      link1.classList.add("navigation-link--loading");

      dispatchEvent(new PageTransitionEvent("pageshow", { persisted: true }));

      expect(link1.classList.contains("navigation-link--loading")).toBe(false);
    });

    it("does not clear loading classes on a regular (non-persisted) pageshow", function () {
      const { link1 } = renderNav();
      link1.classList.add("navigation-link--loading");

      dispatchEvent(new PageTransitionEvent("pageshow", { persisted: false }));

      expect(link1.classList.contains("navigation-link--loading")).toBe(true);
    });
  });

  describe("DOMContentLoaded setup", function () {
    function dispatchDomContentLoaded() {
      document.dispatchEvent(new Event("DOMContentLoaded", { bubbles: true, cancelable: true }));
    }

    it("anchors each subnav-group to its parent navigation-link", function () {
      const { link2 } = renderNav();
      const subnavGroup = document.querySelector(".subnav-group");

      dispatchDomContentLoaded();

      expect(link2.style.anchorName).toBe("--nav-subnav-0");
      expect(subnavGroup.style.positionAnchor).toBe("--nav-subnav-0");
    });

    it("reflects the initial collapsed state on the toggle button", function () {
      const { navToggle } = renderNav({ collapsed: true });

      dispatchDomContentLoaded();

      expect(navToggle.getAttribute("aria-expanded")).toBe("false");
    });

    it("reflects the initial expanded state on the toggle button", function () {
      const { navToggle } = renderNav({ collapsed: false });

      dispatchDomContentLoaded();

      expect(navToggle.getAttribute("aria-expanded")).toBe("true");
    });

    it("adds tooltips upfront when starting collapsed", function () {
      renderNav({ collapsed: true });

      dispatchDomContentLoaded();

      expect(prepareTooltip).toHaveBeenCalled();
    });

    it("does not add tooltips upfront when starting expanded", function () {
      renderNav({ collapsed: false });

      dispatchDomContentLoaded();

      expect(prepareTooltip).not.toHaveBeenCalled();
    });

    it("does nothing when there is no toggle button on the page", function () {
      document.body.innerHTML = `<nav class="navigation"></nav>`;

      expect(() => dispatchDomContentLoaded()).not.toThrow();
    });
  });
});
