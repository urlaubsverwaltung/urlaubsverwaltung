const NONE = "-1";

export default function holidayReplacementChanged(value) {
  const holidayReplacementNoteRow = document.querySelector("#holidayReplacementNoteRow");

  holidayReplacementNoteRow.classList.toggle("hidden", value === NONE);
}
