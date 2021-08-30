const SPECIAL_LEAVE = "2000";
const OVERTIME = "4000";

function vacationTypeChanged(value) {
  const overtime = document.querySelector("#overtime");
  const specialLeave = document.querySelector("#special-leave");

  if (value === SPECIAL_LEAVE) {
    if (overtime) overtime.classList.add("hidden");
    if (specialLeave) specialLeave.classList.remove("hidden");
  } else if (value === OVERTIME) {
    if (overtime) overtime.classList.remove("hidden");
    if (specialLeave) specialLeave.classList.add("hidden");
  } else {
    if (overtime) overtime.classList.add("hidden");
    if (specialLeave) specialLeave.classList.add("hidden");
  }
}

export default vacationTypeChanged;
