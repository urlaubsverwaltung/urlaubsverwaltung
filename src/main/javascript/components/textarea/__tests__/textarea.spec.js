import "..";

describe("textarea", function () {
  afterEach(function () {
    while (document.body.firstChild) {
      document.body.firstChild.remove();
    }
  });

  it("sets 'rows' attribute to 4 when textarea element is focused", function () {
    const textareaOne = renderTextArea();
    const textareaTwo = renderTextArea();

    textareaOne.focus();

    expect(textareaOne.getAttribute("rows")).toBe("4");
    expect(textareaTwo.getAttribute("rows")).toBeNull();
  });

  it("resets 'rows' attribute to default 1 when textarea element is blurred", function () {
    const textareaOne = renderTextArea();
    const textareaTwo = renderTextArea();

    expect(textareaOne.getAttribute("rows")).toBeNull();
    expect(textareaTwo.getAttribute("rows")).toBeNull();

    textareaOne.focus();
    textareaOne.blur();

    expect(textareaOne.getAttribute("rows")).toBe("1");
    expect(textareaTwo.getAttribute("rows")).toBeNull();
  });

  it("resets 'rows' attribute to previous value when textarea element is blurred", function () {
    const textareaOne = renderTextArea();
    textareaOne.setAttribute("rows", "10");

    const textareaTwo = renderTextArea();

    expect(textareaOne.getAttribute("rows")).toBe("10");
    expect(textareaTwo.getAttribute("rows")).toBeNull();

    textareaOne.focus();
    textareaOne.blur();

    expect(textareaOne.getAttribute("rows")).toBe("10");
    expect(textareaTwo.getAttribute("rows")).toBeNull();
  });

  it("does not reset 'rows' attribute to textarea element is blurred but has a value", function () {
    const textareaOne = renderTextArea();
    const textareaTwo = renderTextArea();

    expect(textareaOne.getAttribute("rows")).toBeNull();
    expect(textareaTwo.getAttribute("rows")).toBeNull();

    textareaOne.focus();

    textareaOne.value = "awesome text";

    textareaOne.blur();

    expect(textareaOne.getAttribute("rows")).toBe("4");
    expect(textareaTwo.getAttribute("rows")).toBeNull();
  });

  function renderTextArea() {
    const textarea = document.createElement("textarea");
    document.body.append(textarea);
    return textarea;
  }
});
