import "../checkbox-card/checkbox-card";

describe("checkbox-card", function () {
  it("enables checkbox", function () {
    document.body.innerHTML = `
        <uv-checkbox-card>
          <input type="checkbox" />
          <label>awesome label</label>
          <span id="something-to-click">click me</span>
        </uv-checkbox-card>
    `;

    const checkbox = document.querySelector("input");
    const clickMe = document.querySelector("span");

    expect(checkbox.checked).toBe(false);

    clickMe.click();
    expect(checkbox.checked).toBe(true);
  });

  it("disables checkbox", function () {
    document.body.innerHTML = `
        <uv-checkbox-card>
          <input type="checkbox" checked="checked" />
          <label>awesome label</label>
          <span id="something-to-click">click me</span>
        </uv-checkbox-card>
    `;

    const checkbox = document.querySelector("input");
    const clickMe = document.querySelector("span");

    expect(checkbox.checked).toBe(true);

    clickMe.click();
    expect(checkbox.checked).toBe(false);
  });

  it("dispatches checkbox changed event", function () {
    expect.assertions(2);

    document.body.innerHTML = `
        <uv-checkbox-card>
          <input type="checkbox" />
          <label>awesome label</label>
          <span id="something-to-click">click me</span>
        </uv-checkbox-card>
    `;

    const checkbox = document.querySelector("input");
    const clickMe = document.querySelector("span");

    let eventDispatched = false;

    checkbox.addEventListener("change", () => {
      eventDispatched = true;
    });

    expect(eventDispatched).toBe(false);

    clickMe.click();
    expect(eventDispatched).toBe(true);
  });
});
