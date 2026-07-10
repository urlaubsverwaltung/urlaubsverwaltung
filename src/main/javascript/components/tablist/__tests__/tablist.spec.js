import "../tablist";

describe("tablist", function () {
  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.restoreAllMocks();
  });

  function renderTablist() {
    document.body.innerHTML = `
      <ul role="tablist">
        <li><button role="tab" aria-controls="tab-content-1" class="tab--active">Tab 1</button></li>
        <li><a href="#" role="tab" aria-controls="tab-content-2">Tab 2</a></li>
      </ul>
      <div role="tabpanel" id="tab-content-1" class="tab-panel--active">Tab Content 1</div>
      <div role="tabpanel" id="tab-content-2">Tab Content 2</div>
    `;
  }

  function clickOn(element) {
    const event = new MouseEvent("click", { bubbles: true, cancelable: true });
    element.dispatchEvent(event);
    return event;
  }

  it("activates the clicked tab and deactivates the others", function () {
    renderTablist();

    const [tab1, tab2] = document.querySelectorAll("[role=tab]");
    clickOn(tab2);

    expect(tab1.classList.contains("tab--active")).toBe(false);
    expect(tab2.classList.contains("tab--active")).toBe(true);
  });

  it("shows the matching tab-panel and hides the others", function () {
    renderTablist();

    const tab2 = document.querySelectorAll("[role=tab]")[1];
    clickOn(tab2);

    expect(document.querySelector("#tab-content-1").classList.contains("tab-panel--active")).toBe(false);
    expect(document.querySelector("#tab-content-2").classList.contains("tab-panel--active")).toBe(true);
  });

  it("prevents default navigation when the tab-panel exists", function () {
    renderTablist();

    const tab2 = document.querySelectorAll("[role=tab]")[1];
    const event = clickOn(tab2);

    expect(event.defaultPrevented).toBe(true);
  });

  it("does not prevent default navigation and warns when the tab-panel does not exist", function () {
    renderTablist();
    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});

    const tab2 = document.querySelectorAll("[role=tab]")[1];
    tab2.setAttribute("aria-controls", "does-not-exist");
    const event = clickOn(tab2);

    expect(event.defaultPrevented).toBe(false);
    expect(warnSpy).toHaveBeenCalledWith("Could not find tabPanel with id=%s", "does-not-exist");
  });

  it("does nothing when clicking a non-tab element inside the tablist", function () {
    renderTablist();

    const tabList = document.querySelector("[role=tablist]");
    clickOn(tabList);

    expect(document.querySelector("[role=tab].tab--active").getAttribute("aria-controls")).toBe("tab-content-1");
  });

  it("does nothing when clicking outside of a tablist", function () {
    renderTablist();
    document.body.insertAdjacentHTML("beforeend", `<button id="outside">outside</button>`);

    clickOn(document.querySelector("#outside"));

    expect(document.querySelector("[role=tab].tab--active").getAttribute("aria-controls")).toBe("tab-content-1");
    expect(document.querySelector("#tab-content-1").classList.contains("tab-panel--active")).toBe(true);
  });
});
