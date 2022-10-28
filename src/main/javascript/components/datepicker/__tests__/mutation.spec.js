import { mutation } from "../mutation";

describe("mutation", () => {
  it("notifies when observed attribute is added", (done) => {
    expect.hasAssertions();

    document.body.innerHTML = `<input name="hero" type="text" />`;

    const element = document.querySelector("input");

    mutation(element)
      .attributeChanged(["disabled"])
      .subscribe(function (event) {
        expect(event.target).toBe(element);
        expect(event.data.attributeName).toBe("disabled");
        expect(event.data.oldValue).toBeNull();
        expect(event.target.getAttribute("disabled")).toBe("");
        done();
      });

    element.setAttribute("disabled", "");
  });

  it("notifies when observed attribute value changed", (done) => {
    expect.hasAssertions();

    document.body.innerHTML = `<input name="hero" type="text" />`;

    const element = document.querySelector("input");
    let callCount = 0;

    mutation(element)
      .attributeChanged(["disabled"])
      .subscribe(function (event) {
        callCount++;

        expect(event.target).toBe(element);
        expect(event.data.attributeName).toBe("disabled");

        if (callCount === 1) {
          expect(event.data.oldValue).toBeNull();
          expect(event.target.getAttribute("disabled")).toBe("");
          setTimeout(function () {
            element.setAttribute("disabled", "disabled");
          });
        }

        if (callCount === 2) {
          expect(event.data.oldValue).toBe("");
          expect(event.target.getAttribute("disabled")).toBe("disabled");
          done();
        }
      });

    element.setAttribute("disabled", "");
  });
});
