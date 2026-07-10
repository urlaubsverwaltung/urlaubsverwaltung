import holidayReplacementChanged from "../holiday-replacement-changed";

describe("holiday-replacement-changed", function () {
  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
  });

  function render() {
    document.body.innerHTML = `<div id="holidayReplacementNoteRow"></div>`;
    return document.querySelector("#holidayReplacementNoteRow");
  }

  it("hides the note row when '-1' (none selected) is passed", function () {
    const row = render();

    holidayReplacementChanged("-1");

    expect(row.classList.contains("hidden")).toBe(true);
  });

  it("shows the note row when an actual person id is passed", function () {
    const row = render();
    row.classList.add("hidden");

    holidayReplacementChanged("42");

    expect(row.classList.contains("hidden")).toBe(false);
  });
});
