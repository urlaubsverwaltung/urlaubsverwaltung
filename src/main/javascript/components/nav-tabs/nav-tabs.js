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
          this[activeSymbol].classList.add("border-transparent");
          this[activeSymbol].classList.remove("border-zinc-200");
          previousLink.classList.remove("text-black-almost", "dark:text-zinc-200");
          previousLink.classList.add("text-zinc-400", "dark:text-zinc-500");
          document.querySelector(this[activeSymbol].dataset.content).setAttribute("hidden", "");

          clickedItem.dataset.active = "true";
          clickedItem.classList.remove("border-transparent");
          clickedItem.classList.add("border-zinc-200");
          event.target.classList.add("text-black-almost", "dark:text-zinc-200");
          event.target.classList.remove("text-zinc-400", "dark:text-zinc-500");
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
