import { Selector } from 'testcafe';

export const selectors = Object.freeze({
  yearSelect: Selector('#year-selection'),
  // the first box is the userbox currently
  userBox: Selector('.box'),
  userBoxUsername: Selector('.box '),
});

export const calendarSelectors = Object.freeze({
  next: Selector('#datepicker .datepicker-next'),
  prev: Selector('#datepicker .datepicker-prev'),
  date: Selector(date =>
    document.querySelector(`#datepicker [data-datepicker-date="${date}"]`)
  ),
});

export function ensureYearPicker(t, { year }) {
  return t.expect(selectors.yearSelect.innerText).eql(year);
}

export function ensureUserBox(t, { username, email }) {
  const usernameElement = selectors.userBoxUsername;

  // a[href...] selector does not work in started chrome
  // but why?? using this selector manually works (document.querySelector('a[href="..."]'))

  // email is a mailto link somewhere in the box
  // const emailElement = await selectors.userBox.find(`a[href="mailto:${email}"]`);
  // const emailExists = await emailElement.exists;

  return t
    .expect(usernameElement.innerText).contains(`${username}`)
    // .expect(emailExists).ok()
    // .expect(await emailElement.innerText).eql(email)
}

export default {
  selectors,
  calendarSelectors,
  ensureYearPicker,
  ensureUserBox,
};
