const buttons = [...document.querySelectorAll(".nav-popup-menu-button")];

document.addEventListener("click", function (event) {
  const selectedButton =
    buttons.find((button) => button === event.target) || event.target.closest(".nav-popup-menu-button");
  if (selectedButton) {
    event.preventDefault();
    const menu = document.querySelector(selectedButton.getAttribute("href"));
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
