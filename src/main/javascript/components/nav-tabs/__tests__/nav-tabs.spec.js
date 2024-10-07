import "..";

describe("nav-tabs", function () {
  afterEach(function () {
    while (document.body.firstChild) {
      document.body.firstChild.remove();
    }

    jest.clearAllMocks();
  });

  it("is registered as a 'uv-nav-tabs' custom element", function () {
    const element = customElements.get("uv-nav-tabs");
    expect(element).toBeDefined();
  });

  it("updates look and feel after nav-tab is clicked", function () {
    const div = document.createElement("div");
    div.innerHTML = `
      <ul is="uv-nav-tabs">
        <li class="tw-border-b-4 tw-border-zinc-200" data-content="#content-aaa" data-active="">
          <a href="#content-aaa" class="tw-text-black-almost">
            AAA
          </a>
        </li>
        <li class="tw-border-b-4 tw-border-transparent" data-content="#content-bbb">
          <a href="#content-bbb" class="tw-text-zinc-400">
            BBB
          </a>
        </li>
      </ul>
      <div>
        <div id="content-aaa">
          content AAA
        </div>
        <div id="content-bbb" hidden>
          content BBB
        </div>
      </div>
    `;
    document.body.append(div);

    const navTabA = document.querySelector("li[data-content='#content-aaa']");
    const navTabALink = navTabA.querySelector("a");
    const contentA = document.querySelector("div#content-aaa");

    const navTabB = document.querySelector("li[data-content='#content-bbb']");
    const navTabBLink = navTabB.querySelector("a");
    const contentB = document.querySelector("div#content-bbb");

    navTabBLink.dispatchEvent(new MouseEvent("click", { bubbles: true }));

    expect(navTabA.dataset.active).not.toBeDefined();
    expect(navTabA.classList.contains("tw-border-zinc-200")).toBeFalsy();
    expect(navTabA.classList.contains("tw-border-transparent")).toBeTruthy();
    expect(navTabALink.classList.contains("tw-text-black-almost")).toBeFalsy();
    expect(navTabALink.classList.contains("tw-text-zinc-400")).toBeTruthy();
    expect(contentA.hasAttribute("hidden")).toBeTruthy();

    expect(navTabB.dataset.active).toBeDefined();
    expect(navTabB.classList.contains("tw-border-zinc-200")).toBeTruthy();
    expect(navTabB.classList.contains("tw-border-transparent")).toBeFalsy();
    expect(navTabBLink.classList.contains("tw-text-black-almost")).toBeTruthy();
    expect(navTabBLink.classList.contains("tw-text-zinc-400")).toBeFalsy();
    expect(contentB.hasAttribute("hidden")).toBeFalsy();
  });

  it("does not update look and feel after current active nav-tab is clicked", function () {
    const div = document.createElement("div");
    div.innerHTML = `
      <ul is="uv-nav-tabs">
        <li class="tw-border-b-4 tw-border-zinc-200" data-content="#content-aaa" data-active="">
          <a href="#content-aaa" class="tw-text-black-almost">
            AAA
          </a>
        </li>
        <li class="tw-border-b-4 tw-border-transparent" data-content="#content-bbb">
          <a href="#content-bbb" class="tw-text-zinc-400">
            BBB
          </a>
        </li>
      </ul>
      <div>
        <div id="content-aaa">
          content AAA
        </div>
        <div id="content-bbb" hidden>
          content BBB
        </div>
      </div>
    `;
    document.body.append(div);

    const navTabA = document.querySelector("li[data-content='#content-aaa']");
    const navTabALink = navTabA.querySelector("a");
    const contentA = document.querySelector("div#content-aaa");

    const navTabB = document.querySelector("li[data-content='#content-bbb']");
    const navTabBLink = navTabB.querySelector("a");
    const contentB = document.querySelector("div#content-bbb");

    navTabALink.dispatchEvent(new MouseEvent("click", { bubbles: true }));

    expect(navTabA.dataset.active).toBeDefined();
    expect(navTabA.classList.contains("tw-border-zinc-200")).toBeTruthy();
    expect(navTabA.classList.contains("tw-border-transparent")).toBeFalsy();
    expect(navTabALink.classList.contains("tw-text-black-almost")).toBeTruthy();
    expect(navTabALink.classList.contains("tw-text-zinc-400")).toBeFalsy();
    expect(contentA.hasAttribute("hidden")).toBeFalsy();

    expect(navTabB.dataset.active).not.toBeDefined();
    expect(navTabB.classList.contains("tw-border-zinc-200")).toBeFalsy();
    expect(navTabB.classList.contains("tw-border-transparent")).toBeTruthy();
    expect(navTabBLink.classList.contains("tw-text-black-almost")).toBeFalsy();
    expect(navTabBLink.classList.contains("tw-text-zinc-400")).toBeTruthy();
    expect(contentB.hasAttribute("hidden")).toBeTruthy();
  });

  it("replaces the URL when nav-tab is clicked", function () {
    jest.spyOn(globalThis.history, "replaceState");

    const div = document.createElement("div");
    div.innerHTML = `
      <ul is="uv-nav-tabs">
        <li class="tw-border-b-4 tw-border-zinc-200" data-content="#content-aaa" data-active="">
          <a href="#content-aaa" class="tw-text-black-almost">
            AAA
          </a>
        </li>
        <li class="tw-border-b-4 tw-border-transparent" data-content="#content-bbb">
          <a href="#content-bbb" class="tw-text-zinc-400">
            BBB
          </a>
        </li>
      </ul>
      <div>
        <div id="content-aaa">
          content AAA
        </div>
        <div id="content-bbb" hidden>
          content BBB
        </div>
      </div>
    `;
    document.body.append(div);

    const navTabALink = document.querySelector("li[data-content='#content-aaa'] a");
    const navTabBLink = document.querySelector("li[data-content='#content-bbb'] a");

    navTabBLink.dispatchEvent(new MouseEvent("click", { bubbles: true }));
    expect(globalThis.history.replaceState).toHaveBeenLastCalledWith(
      undefined,
      undefined,
      "http://localhost/#content-bbb",
    );

    navTabALink.dispatchEvent(new MouseEvent("click", { bubbles: true }));
    expect(globalThis.history.replaceState).toHaveBeenLastCalledWith(
      undefined,
      undefined,
      "http://localhost/#content-aaa",
    );
  });

  it("does not replace the URL when the current active nav-tab is clicked", function () {
    jest.spyOn(globalThis.history, "replaceState");

    const div = document.createElement("div");
    div.innerHTML = `
      <ul is="uv-nav-tabs">
        <li class="tw-border-b-4 tw-border-zinc-200" data-content="#content-aaa" data-active="">
          <a href="#content-aaa" class="tw-text-black-almost">
            AAA
          </a>
        </li>
      </ul>
      <div>
        <div id="content-aaa">
          content AAA
        </div>
      </div>
    `;
    document.body.append(div);

    const navTabALink = document.querySelector("li[data-content='#content-aaa'] a");
    navTabALink.dispatchEvent(new MouseEvent("click", { bubbles: true }));

    expect(globalThis.history.replaceState).not.toHaveBeenCalled();
  });
});
