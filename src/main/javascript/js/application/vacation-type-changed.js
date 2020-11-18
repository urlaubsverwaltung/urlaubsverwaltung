const SPECIAL_LEAVE = "2000";
const OVERTIME = "4000";

export default function vacationTypeChanged(value) {
  const reasonElement = document.querySelector("#form-group--reason");
  const hoursElement = document.querySelector("#form-group--hours");

  if (value === SPECIAL_LEAVE) {
    hoursElement.classList.remove("is-required");
    reasonElement.classList.add("is-required");
  } else if (value === OVERTIME) {
    hoursElement.classList.add("is-required");
    reasonElement.classList.remove("is-required");
  } else {
    hoursElement.classList.remove("is-required");
    reasonElement.classList.remove("is-required");
  }
}
