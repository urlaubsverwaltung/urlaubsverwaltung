const avatarlink = document.querySelector("#avatar-link");
const avatarmenu = document.querySelector("#avatar-menu");

document.addEventListener("click", function (event) {
  if (event.target === avatarlink || event.target.closest("#avatar-link")) {
    event.preventDefault();
    avatarmenu.classList.toggle("tw-scale-x-0");
    avatarmenu.classList.toggle("tw-scale-y-0");
  } else {
    avatarmenu.classList.add("tw-scale-x-0", "tw-scale-y-0");
  }
});
