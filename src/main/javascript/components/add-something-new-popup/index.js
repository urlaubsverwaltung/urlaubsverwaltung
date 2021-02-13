const button = document.querySelector("#add-something-new");
const menu = document.querySelector("#add-something-new-menu");

let currentlyVisible = false;

document.addEventListener("click", function (event) {
  if (event.target === button || event.target.closest("#add-something-new")) {
    event.preventDefault();
    menu.classList.toggle("tw-scale-x-0");
    menu.classList.toggle("tw-scale-y-0");
    menu.setAttribute("aria-hidden", currentlyVisible ? "true" : "false");
    button.setAttribute("aria-expanded", currentlyVisible ? "false" : "true");
    currentlyVisible = !currentlyVisible;
  } else {
    menu.classList.add("tw-scale-x-0", "tw-scale-y-0");
    menu.setAttribute("aria-hidden", "true");
    button.setAttribute("aria-expanded", "false");
    currentlyVisible = false;
  }
});
