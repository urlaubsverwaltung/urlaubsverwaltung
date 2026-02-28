import { describe, it, expect } from "vitest";
import { mutation } from "../mutation";

describe("mutation", () => {
  it("notifies when observed attribute is added", async () => {
    document.body.innerHTML = `<input name="hero" type="text" />`;

    const element = document.querySelector("input");
    let expectedEvent;

    mutation(element)
      .attributeChanged(["disabled"])
      .subscribe(function (event) {
        expectedEvent = event;
      });

    element.setAttribute("disabled", "");

    await waitFor(() => Boolean(expectedEvent));
    expect(expectedEvent.target).toBe(element);
    expect(expectedEvent.data.attributeName).toBe("disabled");
    expect(expectedEvent.data.oldValue).toBeNull();
    expect(expectedEvent.target.getAttribute("disabled")).toBe("");
  });

  it("notifies when observed attribute value changed", async () => {
    document.body.innerHTML = `<input name="hero" type="text" />`;

    const element = document.querySelector("input");
    const expectedEvents = [];

    mutation(element)
      .attributeChanged(["disabled"])
      .subscribe(function (event) {
        expectedEvents.push(event);
      });

    // happy-dom requires a value, otherwise oldValue will be null (setting emptyString instead of a defined value...)
    element.setAttribute("disabled", "other");

    await waitFor(() => expectedEvents.length === 1);
    expect(expectedEvents[0].target).toBe(element);
    expect(expectedEvents[0].data.attributeName).toBe("disabled");
    expect(expectedEvents[0].data.oldValue).toBeNull();
    expect(expectedEvents[0].target.getAttribute("disabled")).toBe("other");

    setTimeout(() => {
      element.setAttribute("disabled", "disabled");
    });

    await waitFor(() => expectedEvents.length === 2);
    expect(expectedEvents[1].target).toBe(element);
    expect(expectedEvents[1].data.attributeName).toBe("disabled");
    expect(expectedEvents[1].data.oldValue).toBe("other");
    expect(expectedEvents[1].target.getAttribute("disabled")).toBe("disabled");
  });
});

async function waitFor(callback){
  await new Promise((resolve) => {
    const handle = setInterval(function () {
      if (callback()) {
        clearInterval(handle);
        resolve();
      }
    }, 10);
  });
}
