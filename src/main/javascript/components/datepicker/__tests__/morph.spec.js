import { morphKeepingDatepickers } from "../morph";

describe("morph", () => {
  afterEach(() => {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
  });

  /**
   * The DOM after `createDatepicker` has replaced the server side `input[type=date]`: the id of that input
   * now lives on the inner input of the datepicker, the name on a hidden input of it.
   */
  function hydratedPage() {
    document.body.innerHTML = `
      <form>
        <duet-date-picker
          class="sicknote-extend-button hydrated"
          identifier="extend-to-date-input"
          name="extendToDate"
          min="2024-09-30"
          value="2024-09-30"
        >
          <div class="duet-date">
            <div class="duet-date__input-wrapper">
              <input id="extend-to-date-input" class="duet-date__input" value="30.9.2024" />
              <input type="hidden" name="extendToDate" value="2024-09-30" />
              <button class="duet-date__toggle" type="button"></button>
            </div>
          </div>
        </duet-date-picker>
        <p id="feedback">no preview yet</p>
      </form>
    `;
    return document.body;
  }

  /**
   * The page as the server renders it, with a plain `input[type=date]` instead of the datepicker.
   */
  function serverSidePage({ inputClass = "sicknote-extend-button", feedback = "no preview yet" } = {}) {
    const body = document.createElement("body");
    body.innerHTML = `
      <form>
        <input
          type="date"
          id="extend-to-date-input"
          name="extendToDate"
          class="${inputClass}"
          min="2024-09-30"
          value=""
          data-iso-value="2024-09-30"
        />
        <p id="feedback">${feedback}</p>
      </form>
    `;
    return body;
  }

  it("keeps the datepicker instead of letting the server side date input replace it", () => {
    const currentPage = hydratedPage();

    morphKeepingDatepickers(currentPage, serverSidePage({ feedback: "preview until 2. Oktober" }));

    const datepicker = document.querySelector("duet-date-picker");
    expect(datepicker).not.toBeNull();
    // the datepicker is useless without the input it renders, which carries the id of the server side one
    expect(datepicker.querySelector("input#extend-to-date-input")).not.toBeNull();
    expect(document.querySelector("form").children[0]).toBe(datepicker);
    // the rest of the page is morphed as usual
    expect(document.querySelector("#feedback").textContent).toBe("preview until 2. Oktober");
  });

  it("submits the date of the datepicker only, not a second stale one", () => {
    const currentPage = hydratedPage();

    morphKeepingDatepickers(currentPage, serverSidePage());

    const submitted = [...document.querySelectorAll("form input[name=extendToDate]")];
    expect(submitted).toHaveLength(1);
    expect(submitted[0].value).toBe("2024-09-30");
  });

  it("hands the css classes of the server side date input to the datepicker", () => {
    const currentPage = hydratedPage();

    morphKeepingDatepickers(
      currentPage,
      serverSidePage({ inputClass: "sicknote-extend-button sicknote-extend-button--selected error" }),
    );

    const datepicker = document.querySelector("duet-date-picker");
    expect([...datepicker.classList]).toContain("sicknote-extend-button--selected");
    expect([...datepicker.classList]).toContain("error");
  });

  it("takes the css classes away from the datepicker again", () => {
    const currentPage = hydratedPage();
    document.querySelector("duet-date-picker").classList.add("sicknote-extend-button--selected", "error");

    morphKeepingDatepickers(currentPage, serverSidePage());

    const classes = [...document.querySelector("duet-date-picker").classList];
    expect(classes).not.toContain("sicknote-extend-button--selected");
    expect(classes).not.toContain("error");
    expect(classes).toContain("sicknote-extend-button");
  });

  it("leaves the datepicker without classes when the date input has none", () => {
    const currentPage = hydratedPage();

    morphKeepingDatepickers(currentPage, serverSidePage({ inputClass: "" }));

    expect([...document.querySelector("duet-date-picker").classList]).toEqual(["hydrated"]);
  });

  it("keeps the class duet marks the element it hydrated with", () => {
    const currentPage = hydratedPage();

    morphKeepingDatepickers(currentPage, serverSidePage());

    // without it the element duet renders stays invisible
    expect(document.querySelector("duet-date-picker").classList).toContain("hydrated");
  });

  it("renders the date input of the server when there is no datepicker for it", () => {
    document.body.innerHTML = `<form><p id="feedback">no preview yet</p></form>`;

    morphKeepingDatepickers(document.body, serverSidePage());

    const dateInput = document.querySelector("form input[type=date]");
    expect(dateInput).not.toBeNull();
    expect(dateInput.getAttribute("name")).toBe("extendToDate");
  });
});
