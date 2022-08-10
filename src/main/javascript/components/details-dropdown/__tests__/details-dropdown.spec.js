import "../details-dropdown";

describe("details-dropdown", function () {
  it("closes itself when something on page is clicked", function () {
    document.body.innerHTML = `
        <details is="uv-details-dropdown" open>
            <summary>awesome title</summary>
            <div>
                <p>content content content</p>
                <p>content content content</p>
            </div>
        </details>
    `;

    const detailsDropdown = document.querySelector("[is='uv-details-dropdown']");

    expect(detailsDropdown.open).toBe(true);

    document.body.click();
    expect(detailsDropdown.open).toBe(false);
  });

  it("closes itself when summary is clicked", function () {
    document.body.innerHTML = `
        <details is="uv-details-dropdown" open>
            <summary>awesome title</summary>
            <div>
                <p>content content content</p>
                <p>content content content</p>
            </div>
        </details>
    `;

    const detailsDropdown = document.querySelector("[is='uv-details-dropdown']");
    const summary = detailsDropdown.querySelector("summary");

    expect(detailsDropdown.open).toBe(true);

    summary.click();
    expect(detailsDropdown.open).toBe(false);
  });

  it("closes itself when child of summary is clicked", function () {
    document.body.innerHTML = `
        <details is="uv-details-dropdown" open>
            <summary>
              <span>awesome summary child element</span>
            </summary>
            <div>
                <p>content content content</p>
                <p>content content content</p>
            </div>
        </details>
    `;

    const detailsDropdown = document.querySelector("[is='uv-details-dropdown']");
    const summaryChild = detailsDropdown.querySelector("summary > span");

    expect(detailsDropdown.open).toBe(true);

    summaryChild.click();
    expect(detailsDropdown.open).toBe(false);
  });

  it("does not close itself when content is clicked", function () {
    document.body.innerHTML = `
        <details is="uv-details-dropdown" open>
            <summary>awesome title</summary>
            <div>
                <p>content content content</p>
                <p>content content content</p>
            </div>
        </details>
    `;

    const detailsDropdown = document.querySelector("[is='uv-details-dropdown']");

    expect(detailsDropdown.open).toBe(true);

    const someParagraph = document.querySelector("p");
    someParagraph.click();

    expect(detailsDropdown.open).toBe(true);
  });

  it("closes itself when 'Escape' is pressed", function () {
    document.body.innerHTML = `
        <details is="uv-details-dropdown" open>
            <summary>awesome summary</summary>
            <div>
                <p>content content content</p>
                <p>content content content</p>
            </div>
        </details>
    `;

    const detailsDropdown = document.querySelector("[is='uv-details-dropdown']");

    expect(detailsDropdown.open).toBe(true);

    const keyboardEvent = new KeyboardEvent("keyup", { key: "Escape" });
    document.dispatchEvent(keyboardEvent);

    expect(detailsDropdown.open).toBe(false);
  });

  it("does not close itself when any key other 'Escape' is pressed", function () {
    document.body.innerHTML = `
        <details is="uv-details-dropdown" open>
            <summary>awesome summary</summary>
            <div>
                <p>content content content</p>
                <p>content content content</p>
            </div>
        </details>
    `;

    const detailsDropdown = document.querySelector("[is='uv-details-dropdown']");

    expect(detailsDropdown.open).toBe(true);

    const keyboardEvent = new KeyboardEvent("keyup", { key: "Enter" });
    document.dispatchEvent(keyboardEvent);

    expect(detailsDropdown.open).toBe(true);
  });
});
