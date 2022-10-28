function vacationTypeChanged(value) {
  const overtime = document.querySelector("#overtime");
  const specialLeave = document.querySelector("#special-leave");

  if (value === "SPECIALLEAVE") {
    overtime?.classList.add("hidden");
    specialLeave?.classList.remove("hidden");
  } else if (value === "OVERTIME") {
    overtime?.classList.remove("hidden");
    specialLeave?.classList.add("hidden");
  } else {
    overtime?.classList.add("hidden");
    specialLeave?.classList.add("hidden");
  }
}

export default vacationTypeChanged;
