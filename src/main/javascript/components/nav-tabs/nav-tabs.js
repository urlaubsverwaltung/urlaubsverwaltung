const activeSymbol = Symbol();

class NavTabs extends HTMLUListElement {
  connectedCallback() {
    this[activeSymbol] = this.querySelector("li[data-active]");

    function handleClick(event) {
      if (event.target.tagName === "A") {
        event.preventDefault();

        const clickedItem = event.target.closest("li");

        if (this[activeSymbol] !== clickedItem) {
          const previousLink = this[activeSymbol].querySelector("a");
          delete this[activeSymbol].dataset.active;
          this[activeSymbol].classList.add("tw-border-transparent");
          this[activeSymbol].classList.remove("tw-border-zinc-200");
          previousLink.classList.remove("tw-text-black-almost", "dark:tw-text-zinc-200");
          previousLink.classList.add("tw-text-zinc-400", "dark:tw-text-zinc-500");
          document.querySelector(this[activeSymbol].dataset.content).setAttribute("hidden", "");

          clickedItem.dataset.active = "true";
          clickedItem.classList.remove("tw-border-transparent");
          clickedItem.classList.add("tw-border-zinc-200");
          event.target.classList.add("tw-text-black-almost", "dark:tw-text-zinc-200");
          event.target.classList.remove("tw-text-zinc-400", "dark:tw-text-zinc-500");
          document.querySelector(clickedItem.dataset.content).removeAttribute("hidden");

          this[activeSymbol] = clickedItem;
          history.replaceState(undefined, undefined, event.target.href);
        }
      }
    }

    this.addEventListener("click", handleClick);

    this.cleanup = function () {
      this.removeEventListener("click", handleClick);
    };
  }

  disconnectedCallback() {
    this.cleanup();
  }
}

customElements.define("uv-nav-tabs", NavTabs, { extends: "ul" });
