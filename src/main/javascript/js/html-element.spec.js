import { updateHtmlElementAttributes } from "./html-element";

describe("html-element", function () {
  describe("updateHtmlElementAttributes", function () {
    test("removes 'data-js-no-hidden' attributes", function () {
      const remove = document.createElement("div");
      remove.dataset.jsNoHidden = "true";
      remove.setAttribute("hidden", "hidden");

      const keep = document.createElement("div");
      keep.setAttribute("hidden", "hidden");

      const div = document.createElement("div");
      div.append(remove, keep);

      updateHtmlElementAttributes(div);

      expect(remove.hasAttribute("hidden")).toBe(false);
      expect(keep.hasAttribute("hidden")).toBe(true);
    });

    test("adds 'data-js-class' classes", function () {
      const element = document.createElement("div");
      element.dataset.jsClass = "foo bar";

      const div = document.createElement("div");
      div.append(element);

      updateHtmlElementAttributes(div);

      expect(element.classList.contains("foo")).toBe(true);
      expect(element.classList.contains("bar")).toBe(true);
    });

    test("does not add 'data-js-class' classes to root element", function () {
      const div = document.createElement("div");
      div.dataset.jsClass = "foo";

      updateHtmlElementAttributes(div);

      expect(div.classList.length).toBe(0);
    });

    test("removes 'data-js-class-remove' classes", function () {
      const element = document.createElement("div");
      element.classList.add("foo", "bar", "something-else");
      element.dataset.jsClassRemove = "foo bar";

      const div = document.createElement("div");
      div.append(element);

      updateHtmlElementAttributes(div);

      expect(element.classList.contains("something-else")).toBe(true);
      expect(element.classList.length).toBe(1);
    });

    test("dies not remove 'data-js-class-remove' classes from root element", function () {
      const div = document.createElement("div");
      div.classList.add("foo", "bar", "something-else");
      div.dataset.jsClassRemove = "foo bar";

      updateHtmlElementAttributes(div);

      expect(div.classList.length).toBe(3);
    });
  });
});
