describe("absence-types", function () {
  beforeEach(function () {});

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    jest.resetModules();
  });

  it("does not break when element does not exist", async function () {
    await setupHtml(`<ul></ul>`);
  });

  it("sets enabled state matching the clicked checkbox", async function () {
    await setupHtml(`
      <ul id="absence-type-list">
        <li data-enabled="false">
          <div data-col-status>
            <input type="checkbox" />
          </div>
        </li>
        <li data-enabled="false">
          <div data-col-status>
            <input type="checkbox" />
          </div>
        </li>
      </ul>
    `);

    const clickEvent = new Event("click", { bubbles: true });
    const checkboxes = [...document.querySelectorAll("input[type=checkbox]")];

    checkboxes[1].checked = true;
    checkboxes[1].dispatchEvent(clickEvent);

    expect(document.querySelector("li:nth-child(1)").dataset.enabled).toBe("false");
    expect(document.querySelector("li:nth-child(2)").dataset.enabled).toBe("true");

    checkboxes[0].checked = true;
    checkboxes[0].dispatchEvent(clickEvent);

    expect(document.querySelector("li:nth-child(1)").dataset.enabled).toBe("true");
    expect(document.querySelector("li:nth-child(2)").dataset.enabled).toBe("true");
  });

  it("clears enabled state on table row matching the clicked checkbox", async function () {
    await setupHtml(`
      <list id="absence-type-list">
        <li data-enabled="true">
          <div data-col-status>
            <input type="checkbox" checked />
          </div>
        </li>
        <li data-enabled="true">
          <div data-col-status>
            <input type="checkbox" checked />
          </div>
        </li>
      </list>
    `);

    const clickEvent = new Event("click", { bubbles: true });
    const checkboxes = [...document.querySelectorAll("input[type=checkbox]")];

    checkboxes[1].checked = false;
    checkboxes[1].dispatchEvent(clickEvent);

    expect(document.querySelector("li:nth-child(1)").dataset.enabled).toBe("true");
    expect(document.querySelector("li:nth-child(2)").dataset.enabled).toBe("false");

    checkboxes[0].checked = false;
    checkboxes[0].dispatchEvent(clickEvent);

    expect(document.querySelector("li:nth-child(1)").dataset.enabled).toBe("false");
    expect(document.querySelector("li:nth-child(2)").dataset.enabled).toBe("false");
  });

  it("ignores checkbox clicks when no child of 'data-col-status'", async function () {
    await setupHtml(`
      <ul id="absence-type-list">
        <li data-enabled="false">
          <div data-col-status>
            <!-- empty -->
          </div>
          <div>
            <input type="checkbox" />
          </div>
        </li>
      </ul>
    `);

    const clickEvent = new Event("click", { bubbles: true });
    const checkboxes = [...document.querySelectorAll("input[type=checkbox]")];

    checkboxes[0].checked = true;
    checkboxes[0].dispatchEvent(clickEvent);

    expect(document.querySelector("li:nth-child(1)").dataset.enabled).toBe("false");
  });

  async function setupHtml(html) {
    document.body.innerHTML = html;
    await import("../absence-types");
  }
});
