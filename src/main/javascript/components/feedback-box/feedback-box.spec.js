import "./feedback-box";

describe("feedback-box", function () {
  beforeEach(function () {
    vi.useFakeTimers();
  });

  afterEach(function () {
    document.body.replaceChildren();
    vi.useRealTimers();
  });

  function sut() {
    return document.querySelector("#feedback-1");
  }

  function render() {
    document.body.innerHTML = `
      <div id="feedback-1" is="uv-feedback-box" class="uv-feedback-box alert" data-test-id="feedback-box">
        <div id="message">some message</div>
        <div>
          <button id="close-button" command="close" commandfor="feedback-1">
            <svg id="close-icon"></svg>
            <span class="sr-only">close</span>
          </button>
        </div>
      </div>
    `;
  }

  it("is registered as a 'uv-feedback-box' custom element", function () {
    const element = customElements.get("uv-feedback-box");
    expect(element).toBeDefined();
  });

  it("removes the element after clicking the close button (fallback delay, no transitionend)", async function () {
    render();
    expect(sut()).not.toBeNull();

    document.querySelector("#close-button").click();
    expect(sut()).not.toBeNull();

    await vi.advanceTimersByTimeAsync(50);

    expect(sut()).toBeNull();
  });

  it("removes the element when clicking a child of the close button", async function () {
    render();

    document.querySelector("#close-icon").dispatchEvent(new MouseEvent("click", { bubbles: true }));
    await vi.advanceTimersByTimeAsync(50);

    expect(sut()).toBeNull();
  });

  it("adds the fade-out class synchronously when the close button is clicked", function () {
    render();

    document.querySelector("#close-button").click();

    expect(sut().classList.contains("uv-feedback-box--fade-out")).toBe(true);
  });

  it("does not remove the element when clicking somewhere else inside it", async function () {
    render();

    document.querySelector("#message").click();
    await vi.advanceTimersByTimeAsync(1000);

    expect(sut()).not.toBeNull();
  });

  it("removes the element as soon as 'transitionend' fires, without waiting for the fallback delay", async function () {
    render();

    document.querySelector("#close-button").click();
    sut().dispatchEvent(new Event("transitionend"));

    await vi.advanceTimersByTimeAsync(0);

    expect(sut()).toBeNull();
  });

  it("remove() returns undefined synchronously, matching the overridden Element#remove() contract", function () {
    render();

    const returnValue = sut().remove();

    expect(returnValue).toBeUndefined();
  });

  it("clicking close on one feedback box does not remove another one", async function () {
    render();
    document.body.insertAdjacentHTML(
      "beforeend",
      `
        <div id="feedback-2" is="uv-feedback-box" class="uv-feedback-box alert">
          <div>another message</div>
        </div>
      `,
    );

    document.querySelector("#close-button").click();
    await vi.advanceTimersByTimeAsync(50);

    expect(sut()).toBeNull();
    expect(document.querySelector("#feedback-2")).not.toBeNull();
  });
});
