describe("special-leave-table", function () {
  beforeEach(function () {});

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    jest.resetModules();
  });

  it("does not break when table does not exist", async function () {
    await setupTableHtml(`<table></table>`);
  });

  it("sets enabled state on table row matching the clicked checkbox", async function () {
    await setupTableHtml(`
      <table id="special-leave-table">
        <tbody>
          <tr data-enabled="false">
            <td data-col-status>
              <input type="checkbox" />
            </td>
          </tr>
          <tr data-enabled="false">
            <td data-col-status>
              <input type="checkbox" />
            </td>
          </tr>
        </tbody>
      </table>
    `);

    const clickEvent = new Event("click", { bubbles: true });
    const checkboxes = [...document.querySelectorAll("input[type=checkbox]")];

    checkboxes[1].checked = true;
    checkboxes[1].dispatchEvent(clickEvent);

    expect(document.querySelector("tr:nth-child(1)").dataset.enabled).toBe("false");
    expect(document.querySelector("tr:nth-child(2)").dataset.enabled).toBe("true");

    checkboxes[0].checked = true;
    checkboxes[0].dispatchEvent(clickEvent);

    expect(document.querySelector("tr:nth-child(1)").dataset.enabled).toBe("true");
    expect(document.querySelector("tr:nth-child(2)").dataset.enabled).toBe("true");
  });

  it("clears enabled state on table row matching the clicked checkbox", async function () {
    await setupTableHtml(`
      <table id="special-leave-table">
        <tbody>
          <tr data-enabled="true">
            <td data-col-status>
              <input type="checkbox" checked />
            </td>
          </tr>
          <tr data-enabled="true">
            <td data-col-status>
              <input type="checkbox" checked />
            </td>
          </tr>
        </tbody>
      </table>
    `);

    const clickEvent = new Event("click", { bubbles: true });
    const checkboxes = [...document.querySelectorAll("input[type=checkbox]")];

    checkboxes[1].checked = false;
    checkboxes[1].dispatchEvent(clickEvent);

    expect(document.querySelector("tr:nth-child(1)").dataset.enabled).toBe("true");
    expect(document.querySelector("tr:nth-child(2)").dataset.enabled).toBe("false");

    checkboxes[0].checked = false;
    checkboxes[0].dispatchEvent(clickEvent);

    expect(document.querySelector("tr:nth-child(1)").dataset.enabled).toBe("false");
    expect(document.querySelector("tr:nth-child(2)").dataset.enabled).toBe("false");
  });

  it("ignores checkbox clicks when no child of 'data-col-status'", async function () {
    await setupTableHtml(`
      <table id="special-leave-table">
        <tbody>
          <tr data-enabled="false">
            <td data-col-status>
              <!-- empty -->
            </td>
            <td>
              <input type="checkbox" />
            </td>
          </tr>
        </tbody>
      </table>
    `);

    const clickEvent = new Event("click", { bubbles: true });
    const checkboxes = [...document.querySelectorAll("input[type=checkbox]")];

    checkboxes[0].checked = true;
    checkboxes[0].dispatchEvent(clickEvent);

    expect(document.querySelector("tr:nth-child(1)").dataset.enabled).toBe("false");
  });

  async function setupTableHtml(html) {
    document.body.innerHTML = html;
    await import("../special-leave-table");
  }
});
