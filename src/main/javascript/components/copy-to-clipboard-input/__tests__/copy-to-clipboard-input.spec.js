import "..";

describe("copy-to-clipboard-input", () => {
  const { clipboard } = globalThis.navigator;

  beforeEach(() => {
    delete globalThis.navigator.clipboard;
    globalThis.navigator.clipboard = {
      writeText: jest.fn(() => Promise.resolve()),
    };
  });

  afterEach(() => {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }

    globalThis.navigator.clipboard = clipboard;

    jest.clearAllMocks();
  });

  test("renders", () => {
    document.body.innerHTML = `<div is="uv-copy-to-clipboard-input"><input type="text"></div>`;

    expect(document.body.innerHTML).toBe(
      `<div is="uv-copy-to-clipboard-input" class="uv-input-group"><input type="text" tabindex="-1"><button class="uv-input-group-addon button m-0 border-0 outline-hidden button--no-hover" title="undefined" data-placement="bottom"><svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="16px" height="16px" class="w-4 h-4 stroke-2" role="img" aria-hidden="true" focusable="false"><path stroke-linecap="round" stroke-linejoin="round" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3"></path></svg></button></div>`,
    );
  });

  test("copies input text value into clipboard on button click", () => {
    document.body.innerHTML = `<div is="uv-copy-to-clipboard-input"><input type="text" value="awesome text"></div>`;

    expect(navigator.clipboard.writeText).not.toHaveBeenCalled();

    const button = document.querySelector("button");
    button.click();

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith("awesome text");
  });

  test("selects whole input text when input is focused", () => {
    jest.spyOn(HTMLInputElement.prototype, "setSelectionRange");

    document.body.innerHTML = `<div is="uv-copy-to-clipboard-input"><input type="text" value="awesome text"></div>`;

    expect(HTMLInputElement.prototype.setSelectionRange).not.toHaveBeenCalled();

    const input = document.querySelector("input");
    input.dispatchEvent(new Event("focus"));

    expect(HTMLInputElement.prototype.setSelectionRange).toHaveBeenCalledWith(0, 12);
  });

  test("selects whole input text when button is focused", () => {
    jest.spyOn(HTMLInputElement.prototype, "setSelectionRange");

    document.body.innerHTML = `<div is="uv-copy-to-clipboard-input"><input type="text" value="awesome text"></div>`;

    expect(HTMLInputElement.prototype.setSelectionRange).not.toHaveBeenCalled();

    const button = document.querySelector("button");
    button.dispatchEvent(new Event("focus"));

    expect(HTMLInputElement.prototype.setSelectionRange).toHaveBeenCalledWith(0, 12);
  });
});
