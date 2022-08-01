const buttons = [...document.querySelectorAll(".nav-popup-menu-button")];
// const menu = document.querySelector("#avatar-menu");

document.addEventListener("click", function (event) {
  const button = buttons.find((button) => button === event.target) || event.target.closest(".nav-popup-menu-button");
  console.log(button);
  if (button) {
    event.preventDefault();
    const menu = document.querySelector(button.getAttribute("href"));
    for (const otherMenu of document.querySelectorAll(".nav-popup-menu.visible")) {
      if (otherMenu !== menu) {
        // only one menu should be opened
        otherMenu.classList.remove("visible");
      }
    }
    menu.classList.toggle("visible");
  } else {
    // clicked anywhere else -> hide menu
    for (const menu of document.querySelectorAll(".nav-popup-menu.visible")) {
      menu.classList.remove("visible");
    }
  }
});
